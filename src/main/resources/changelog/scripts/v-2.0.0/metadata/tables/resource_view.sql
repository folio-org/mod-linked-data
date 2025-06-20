--liquibase formatted sql

--changeset resource_view dbms:postgresql

create or replace view resource_view as
select
  r.resource_hash,
  export_subgraph(r.resource_hash, 5) as resource_subgraph
from
  resources r;

comment on view resource_view is 'JSON for every resource in the resources table, select * where resource_hash = :resource_id';

--rollback drop view if exists resource_view;