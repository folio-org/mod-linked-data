--liquibase formatted sql

--changeset resource_subgraph dbms:postgresql

create or replace function resource_subgraph(id int8, max_depth int)
returns table (
  subject int8,
  predicate text,
  object int8,
  depth int
) as $$
begin
  return query
  with recursive subgraph(subject, predicate, object, doc, depth, is_cycle, path) as (
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
      resources.resource_hash = $1
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
        on resource_edges.predicate_hash = predicate_lookup.predicate_hash,
      inner join subgraph
        on resources.resource_hash = subgraph.object
    where
      not is_cycle
  )
  select
    s.subject, s.predicate, s.object, s.depth
  from
    subgraph s
  where
    s.depth <= $2;
end $$ language plpgsql;

create type export_doc as (
  id int8,
  label text,
  doc jsonb
);

create type export_triple as (
  subject int8,
  predicate text,
  object int8,
  depth int
);

create or replace function export_resource_edges(
  id int8,
  depth int,
  max_depth int,
  docs export_doc[],
  triples export_triple[]
) returns jsonb as $$
declare
  local_doc jsonb;
begin
  if depth = max_depth
  then
    select
      to_jsonb(array[$1::text]) into local_doc;
  else
  select
    jsonb_build_object(
      'id', d.id::text,
      'doc', d.doc,
      'label', d.label,
      'outgoingEdges', jsonb_object_agg(
        s.predicate, array[coalesce(export_resource_edges(
          s.object,
          $2 + 1,
          $3,
          $4,
          $5
        ), to_jsonb(array[$1::text]))]
      )
    ) into local_doc
  from
    unnest(docs) d,
    unnest(triples) s
  where
    d.id = $1
    and d.id = s.subject
  group by d.id, d.label, d.doc;
  end if;
  return local_doc;
end $$ language plpgsql;

create or replace function export_subgraph(id int8, max_depth int) returns jsonb as $$
declare
  subgraph_doc jsonb;
begin
  with subgraph_set as (
    select
      subgraph.subject,
      subgraph.predicate,
      subgraph.object,
      subgraph.depth
    from
      resource_subgraph($1, $2) as subgraph
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
      $1,
      1,
      $2,
      array_agg(row(d.id, d.label, d.doc)::export_doc),
      array_agg(row(s.subject, s.predicate, s.object, s.depth)::export_triple)
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