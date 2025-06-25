--liquibase formatted sql

--changeset export_resources dbms:postgresql

create or replace view export_resources as
  select
    r.resource_hash,
    export_subgraph(r.resource_hash, 4) as resource_subgraph
  from
    resources r;

comment on view export_resources is 'Export JSON for resources to a depth of 4: select * from resource_view where resource_hash = :resource_id';

--rollback drop view if exists export_resources;