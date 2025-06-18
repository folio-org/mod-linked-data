--liquibase formatted sql

--changeset export_resource_view dbms:postgresql

create or replace view export_resource_view as
select
  subgraph.subject,
  subgraph.predicate,
  subgraph.object,
  subgraph.doc,
  subgraph.depth
from
  resources r,
  resource_subgraph(r.resource_hash, 5) as subgraph
where
  r.resource_hash = subgraph.subject;

