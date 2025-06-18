--liquibase formatted sql

--changeset export_resource_subgraph dbms:postgresql

create or replace function resource_subgraph(id int8, max_depth int)
returns table (
  subject int8,
  predicate text,
  object int8,
  doc jsonb,
  depth int)
as $$
begin
  return query
  with recursive subgraph(subject, predicate, object, doc, depth, is_cycle, path) as (
    select
      resources.resource_hash,
      predicate_lookup.predicate,
      resource_edges.target_hash,
      resources.doc,
      1,
      false,
      array[resource_hash]
    from
      resources,
      resource_edges,
      predicate_lookup
    where
      resource_edges.source_hash = resources.resource_hash
      and resource_edges.predicate_hash = predicate_lookup.predicate_hash
      and resources.resource_hash = $1
    union all
    select
      resources.resource_hash,
      predicate_lookup.predicate,
      resource_edges.target_hash,
      resources.doc,
      subgraph.depth + 1,
      resources.resource_hash = any(path),
      path || resources.resource_hash
    from
      resources,
      resource_edges,
      predicate_lookup,
      subgraph
    where
      resource_edges.source_hash = resources.resource_hash
      and resource_edges.predicate_hash = predicate_lookup.predicate_hash
      and resources.resource_hash = subgraph.object
      and not is_cycle
  )
  select
    s.subject, s.predicate, s.object, s.doc, s.depth
  from
    subgraph s
  where
    s.depth <= $2;
end $$ language plpgsql;