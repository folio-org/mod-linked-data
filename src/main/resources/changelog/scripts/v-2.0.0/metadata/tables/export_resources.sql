--liquibase formatted sql

--changeset export_resources dbms:postgresql

create or replace view export_resources as
  select
    r.resource_hash,
    fm.inventory_id,
    export_subgraph(r.resource_hash, 7) as resource_subgraph
  from
    resources r
      left outer join folio_metadata as fm
      on r.resource_hash = fm.resource_hash;

comment on view export_resources is 'Export JSON for resources to a depth of 7: select * from resource_view where resource_hash = :resource_id';

--rollback drop view if exists export_resources;