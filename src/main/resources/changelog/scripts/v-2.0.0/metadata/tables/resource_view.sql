--liquibase formatted sql

--changeset resource_view dbms:postgresql

create or replace view resource_view as
select
  r.resource_hash,
  subgraph.subject,
  subgraph.predicate,
  subgraph.object,
  subgraph.doc,
  subgraph.depth
from
  resources r
  join lateral resource_subgraph(r.resource_hash, 5) as subgraph on true;

comment on view resource_view is 'Resource-centric subgraph for every resource in the resources table, select * where resource_hash = :resource_id';

--rollback drop view if exists resource_view;