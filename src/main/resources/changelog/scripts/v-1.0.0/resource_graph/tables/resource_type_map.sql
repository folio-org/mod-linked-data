--liquibase formatted sql

--changeset dfeeney@ebsco.com:3.6_resource_type_map dbms:postgresql
create table if not exists resource_type_map (
  resource_hash bigint references resources(resource_hash) on delete cascade,
  type_hash bigint references type_lookup(type_hash),
  primary key(resource_hash, type_hash)
) partition by hash(resource_hash);

create index if not exists resource_type_map_type_hash_idx on resource_type_map(type_hash);

comment on table resource_type_map is 'Map of all types to resources';

--rollback drop table if exists resource_type_map;
