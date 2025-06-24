--liquibase formatted sql

--changeset resource_subgraph dbms:postgresql

create type export_doc as (
  id int8,
  label text,
  doc jsonb
);

create type export_triple as (
  subject int8,
  predicate text,
  objects int8[],
  depth int
);

create or replace function resource_subgraph(
  v_id int8,
  v_max_depth int
) returns setof export_triple as $$
begin
  return query
  with recursive subgraph(subject, predicate, object, depth, is_cycle, path) as (
    select
      resources.resource_hash,
      predicate_lookup.predicate,
      resource_edges.target_hash,
      1,
      false,
      array[resource_hash]
    from
      resources
      inner join resource_edges
        on resources.resource_hash = resource_edges.source_hash
      inner join predicate_lookup
        on resource_edges.predicate_hash = predicate_lookup.predicate_hash
    where
      resources.resource_hash = v_id
    union all
    select
      resources.resource_hash,
      predicate_lookup.predicate,
      resource_edges.target_hash,
      subgraph.depth + 1,
      resources.resource_hash = any(path),
      path || resources.resource_hash
    from
      resources
      inner join resource_edges
        on resources.resource_hash = resource_edges.source_hash
      inner join predicate_lookup
        on resource_edges.predicate_hash = predicate_lookup.predicate_hash
      inner join subgraph
        on resources.resource_hash = subgraph.object
    where
      not is_cycle
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
  group by
    s.subject,
    s.predicate,
    s.depth;
end $$ language plpgsql;

create or replace function export_resource_edges(
  v_id int8,
  v_depth int,
  v_max_depth int,
  v_docs export_doc[],
  v_triples export_triple[]
) returns jsonb as $$
declare
  local_doc jsonb;
begin
  if v_depth = v_max_depth
  then
    select
      jsonb_build_object('id', v_id::text) into local_doc;
  else
  with expanded_objects as (
    select
      subject,
      predicate,
      depth,
      array_agg(coalesce(export_resource_edges(
        o,
        v_depth + 1,
        v_max_depth,
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
      'outgoingEdges', jsonb_object_agg(
        eos.predicate, eos.expansion
      )
    ) into local_doc
  from
    unnest(v_docs) d
      inner join expanded_objects as eos
      on d.id = eos.subject
  where
    d.id = v_id
  group by
    d.id,
    d.label,
    d.doc;
  end if;
  return local_doc;
end $$ language plpgsql;

create or replace function export_subgraph(
  v_id int8,
  v_max_depth int
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
      resource_subgraph(v_id, v_max_depth) as subgraph
  ),
  docs_set as (
    select
      r.resource_hash as id,
      r.doc,
      r.label,
      jsonb_agg(type_lookup.type_uri) as types
    from
      resources r
      inner join resource_type_map as rtm
        on rtm.resource_hash = r.resource_hash
      inner join type_lookup
        on rtm.type_hash = type_lookup.type_hash
    where
      r.resource_hash in (
        select
          distinct subject
        from
          subgraph_set
      )
    group by
      r.resource_hash
  )
  select
    export_resource_edges(
      v_id,
      1,
      v_max_depth,
      array_agg(row(d.id, d.label, d.doc)::export_doc),
      array_agg(row(s.subject, s.predicate, s.objects, s.depth)::export_triple)
    ) into subgraph_doc
  from
    docs_set d,
    subgraph_set s;
  return subgraph_doc;
end $$ language plpgsql;

comment on function resource_subgraph(int8, int) is 'Recursively export the graph starting from id, to the depth max_depth, with no cycles as a set';
comment on type export_doc is 'Helper type to represent resource properties with literal values';
comment on type export_triple is 'Helper type to represent resource properties with object values';
comment on function export_resource_edges(int8, int, int, export_doc[], export_triple[]) is 'Recursive function to export as JSON a resource, its properties, and objects, calling itself on objects to a depth of max_depth';
comment on function export_subgraph(int8, int) is 'Export resource subgraph as an aggregated JSONB document';

--rollback drop function if exists resource_subgraph(int8, int);