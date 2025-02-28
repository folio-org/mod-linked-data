--liquibase formatted sql

--changeset pkjacob@ebsco.com:3.5_folio_metadata dbms:postgresql
create table if not exists folio_metadata (
     resource_hash bigint primary key references resources(resource_hash),
     inventory_id text null,
     srs_id text null,
     source resource_source null,
     suppress_from_discovery boolean null,
     staff_suppress boolean null,
     constraint unique (resource_hash, srs_id),
     constraint unique (resource_hash, inventory_id)
  ) partition by hash(resource_hash);

create index if not exists folio_metadata_inventory_id_idx on folio_metadata(inventory_id);
create index if not exists folio_metadata_srs_id_idx on folio_metadata(srs_id);

comment on table folio_metadata is 'Stores FOLIO metadata of LD resources';
comment on column folio_metadata.resource_hash is 'The unique hash identifier for the resource';
comment on column folio_metadata.inventory_id is 'ID of the inventory in FOLIO Inventory application';
comment on column folio_metadata.srs_id is 'ID of the source record in FOLIO SRS application';
comment on column folio_metadata.source is 'Source of the instance resource (ex. LINKED_DATA, MARC)';
comment on column folio_metadata.suppress_from_discovery is 'Suppress From Discovery value';
comment on column folio_metadata.staff_suppress is 'Staff Suppress value';

--rollback drop table if exists folio_metadata;
