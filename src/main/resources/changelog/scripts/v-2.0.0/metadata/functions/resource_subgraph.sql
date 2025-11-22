--liquibase formatted sql

--changeset resource_subgraph dbms:postgresql splitStatements:false

create type export_doc as (
  id       bigint,
  label    text,
  types    jsonb,
  doc      jsonb
);

create type export_triple as (
  subject   bigint,
  predicate text,
  objects   bigint[],
  depth     integer
);

comment on type export_doc is 'Helper type to represent resource properties with literal values';
comment on type export_triple is 'Helper type to represent resource properties with object values';

do $do$
  begin
  execute format($format$
    -- Iteratively retrieve triples from the resources and resource_edges tables
    -- to the specified depth. Avoid any cycles by tracking the path to each
    -- subject and stopping at either the appropriate depth or if a node is
    -- repeated. Triples sharing the same subject and predicate use an array to
    -- output a list of objects because Postgres JSONB aggregation does not merge
    -- on shared keys but replaces instead.
    create or replace function %1$I.resource_subgraph(
      v_id bigint,
      v_max_depth integer
    ) returns setof %1$I.export_triple as $$
    begin
      return query
      with recursive subgraph(subject, predicate, object, depth, is_cycle, path) as (
        select
          resources.resource_hash,
          predicate_lookup.predicate,
          resource_edges.target_hash,
          1,
          false,
          array[resources.resource_hash]
        from
          %1$I.resources
          inner join %1$I.resource_edges
            on resources.resource_hash = resource_edges.source_hash
          inner join %1$I.predicate_lookup
            on resource_edges.predicate_hash = predicate_lookup.predicate_hash
        where
          resources.resource_hash = v_id
        union all
        select
          resources.resource_hash,
          predicate_lookup.predicate,
          resource_edges.target_hash,
          subgraph.depth + 1,
          resources.resource_hash = any(subgraph.path),
          subgraph.path || resources.resource_hash
        from
          %1$I.resources
          inner join %1$I.resource_edges
            on resources.resource_hash = resource_edges.source_hash
          inner join %1$I.predicate_lookup
            on resource_edges.predicate_hash = predicate_lookup.predicate_hash
          inner join subgraph
            on resources.resource_hash = subgraph.object
            and not subgraph.is_cycle
      )
      select
        s.subject,
        s.predicate,
        array_agg(s.object) as objects,
        s.depth
      from
        subgraph s
      where
        s.depth <= v_max_depth
        and not s.is_cycle
      group by
        s.subject,
        s.predicate,
        s.depth;
    end $$
    language plpgsql;

    -- Recursive function to expand triple objects into their own subgraph.
    -- Postgres does not support nested aggregate functions, so object arrays
    -- must be expanded and handled separately from the main query. Some
    -- nodes do not have any outgoing edges, so a coalesce filter is used to
    -- substitute an empty object for those cases, as the build object function
    -- does not allow null properties.
    create or replace function %1$I.export_resource_edges(
      v_id bigint,
      v_depth integer,
      v_max_depth integer,
      v_path bigint[],
      v_docs %1$I.export_doc[],
      v_triples %1$I.export_triple[]
    ) returns jsonb as $$
    declare
      local_doc jsonb;
    begin
      if v_depth = v_max_depth + 1 or v_id = any(v_path)
      then
        select
          jsonb_build_object('id', v_id::text) into local_doc;
      else
      with expanded_objects as (
        select
          subject,
          predicate,
          depth,
          array_agg(coalesce(%1$I.export_resource_edges(
            o,
            v_depth + 1,
            v_max_depth,
            v_id || v_path,
            v_docs,
            v_triples
          ), jsonb_build_object('id', o::text))) as expansion
        from
          (
            select
              s.subject,
              s.predicate,
              s.depth,
              o
            from
              unnest(v_triples) s
                cross join lateral unnest(s.objects) as o
            where
              s.subject = v_id
            group by
              s.subject,
              s.predicate,
              s.depth,
              o
            ) deduped
        group by
          subject,
          predicate,
          depth
      )
      select
        jsonb_build_object(
          'id', d.id::text,
          'doc', d.doc,
          'label', d.label,
          'types', d.types,
          'outgoingEdges', coalesce(jsonb_object_agg(
            eos.predicate, eos.expansion
          ) filter (where eos.predicate is not null), '{}')
        ) into local_doc
      from
        unnest(v_docs) d
          left outer join expanded_objects as eos
          on d.id = eos.subject
      where
        d.id = v_id
      group by
        d.id,
        d.label,
        d.types,
        d.doc;
      end if;
      return local_doc;
    end $$
    language plpgsql;

    -- A small wrapper method to insert the FOLIO inventory UUID into an
    -- instance's admin metadata. This is not stored in the graph and
    -- needs to be pulled in separately. It has no effect on the resource
    -- hash of the admin metadata resource.
    -- This will have no effect if the path to admin metadata does not
    -- already exist. The lack of an HRID in the graph implies the connection
    -- between the resource and FOLIO Inventory is missing altogether
    -- and wouldn't actually reach this point anyways.
    create or replace function %1$I.append_inventory_uuid(
      v_id bigint,
      v_subgraph jsonb
    ) returns jsonb as $$
    declare
      amended_subgraph jsonb;
    begin
      case
        when jsonb_path_exists(v_subgraph, '$.outgoingEdges."http://bibfra.me/vocab/library/adminMetadata"[0].doc."http://bibfra.me/vocab/library/controlNumber"[0]')
        then
          select
            jsonb_insert(
              v_subgraph,
              '{
                outgoingEdges,
                http://bibfra.me/vocab/library/adminMetadata,
                0,
                doc,
                http://bibfra.me/vocab/lite/folioInventoryId
              }',
              (select jsonb_build_array(inventory_id) from folio_metadata where resource_hash = v_id),
              true
            ) into amended_subgraph;
        else
          select v_subgraph into amended_subgraph;
      end case;
      return amended_subgraph;
    end $$
    language plpgsql;

    -- Primary function exporting a JSON subgraph starting with the given
    -- subject to the specified depth.
    create or replace function %1$I.export_subgraph(
      v_id bigint,
      v_max_depth integer
    ) returns jsonb as $$
    declare
      subgraph_doc jsonb;
    begin
      with subgraph_set as (
        select
          subgraph.subject,
          subgraph.predicate,
          subgraph.objects,
          subgraph.depth
        from
          %1$I.resource_subgraph(v_id, v_max_depth) as subgraph
      ),
      docs_set as (
        select
          r.resource_hash as id,
          r.doc,
          r.label,
          jsonb_agg(type_lookup.type_uri) as types
        from
          %1$I.resources r
          inner join %1$I.resource_type_map as rtm
            on rtm.resource_hash = r.resource_hash
          inner join %1$I.type_lookup
            on rtm.type_hash = type_lookup.type_hash
        where
          r.resource_hash in (
            select
              distinct subject
            from
              subgraph_set
            union
            select
              distinct unnest(objects)
            from
              subgraph_set
          )
        group by
          r.resource_hash
      )
      select
        %1$I.append_inventory_uuid(
          v_id,
          jsonb_strip_nulls(%1$I.export_resource_edges(
            v_id,
            1,
            v_max_depth,
            array[]::bigint[],
            (select array_agg(row(id, label, types, doc)::%1$I.export_doc) from docs_set),
            (select array_agg(row(subject, predicate, objects, depth)::%1$I.export_triple) from subgraph_set)
          ))
        ) into subgraph_doc;
      return subgraph_doc;
    end $$
    language plpgsql;

    comment on function %1$I.resource_subgraph(bigint, integer) is 'Recursively export the graph starting from id, to the depth max_depth, with no cycles, as a set';
    comment on function %1$I.export_resource_edges(bigint, integer, integer, bigint[], %1$I.export_doc[], %1$I.export_triple[]) is 'Recursive function to export as JSON a resource, its properties, and objects, calling itself on objects to a depth of max_depth, with no cycles';
    comment on function %1$I.append_inventory_uuid(bigint, jsonb) is 'Insert FOLIO inventory UUID into final JSONB document';
    comment on function %1$I.export_subgraph(bigint, integer) is 'Export resource subgraph as an aggregated JSONB document';
  $format$, CURRENT_SCHEMA);
  end;
$do$;

--rollback drop type if exists export_doc;
--rollback drop type if exists export_triple;
--rollback drop function if exists resource_subgraph(bigint, integer);
--rollback drop function if exists export_resource_edges(bigint, integer, integer, bigint[], export_doc[], export_triple[]);
--rollback drop function if exists append_inventory_uuid(bigint, jsonb);
--rollback drop function if exists export_subgraph(bigint, integer);