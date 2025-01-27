create table if not exists raw_marcs (
     resource_hash bigint primary key references resources(resource_hash),
     content jsonb null
  );

comment on table raw_marcs is 'Store unmapped MARC records associated with an Instance resource. Applicable only for Instance resources';
comment on column raw_marcs.resource_hash is 'The unique hash identifier for the resource';
comment on column raw_marcs.content is 'JSON representation of MARC record';
