--liquibase formatted sql

--changeset export_resources_with_inventory dbms:postgresql

create or replace view export_resources as
  select
    r.resource_hash,
    export_subgraph(r.resource_hash, 7) as resource_subgraph,
    fm.inventory_id
  from
    resources r
      left outer join folio_metadata as fm
      on r.resource_hash = fm.resource_hash;

--rollbackSqlFile path:tables/export_resources.sql relativeToChangelogFile:true