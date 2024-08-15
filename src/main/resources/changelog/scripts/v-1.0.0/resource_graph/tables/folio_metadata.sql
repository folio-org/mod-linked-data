create table if not exists folio_metadata (
     resource_hash bigint primary key references resources(resource_hash),
     inventory_id text null,
     srs_id text null,
     source resource_source null
  );

create index if not exists folio_metadata_inventory_id_idx on folio_metadata(inventory_id);

comment on table folio_metadata is 'Stores FOLIO metadata of LD resources';
comment on column folio_metadata.resource_hash is 'The unique hash identifier for the resource';
comment on column folio_metadata.inventory_id is 'ID of the inventory in FOLIO Inventory application';
comment on column folio_metadata.srs_id is 'ID of the source record in FOLIO SRS application';
comment on column folio_metadata.source is 'Source of the instance resource (ex. LINKED_DATA, MARC)';
