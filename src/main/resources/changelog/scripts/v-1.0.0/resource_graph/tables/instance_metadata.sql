create table if not exists instance_metadata (
     resource_hash bigint primary key references resources(resource_hash),
     inventory_id text null,
     srs_id text null,
     source resource_source null
  );

comment on table instance_metadata is 'Metadata for an instance resource';
comment on column instance_metadata.resource_hash is 'The unique hash identifier for the resource';
comment on column instance_metadata.inventory_id is 'ID of the instance in FOLIO inventory application';
comment on column instance_metadata.srs_id is 'ID of the instance in FOLIO SRS application';
comment on column instance_metadata.source is 'Source of the instance resource';
