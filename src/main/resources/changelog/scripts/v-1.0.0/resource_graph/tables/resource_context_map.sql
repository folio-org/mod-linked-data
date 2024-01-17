--liquibase formatted sql

--changeset dfeeney@ebsco.com:3.7_resource_context_map dbms:postgresql

create table if not exists resource_context_map (
  resource_hash bigint references resources(resource_hash) on delete cascade,
  context_hash bigint references context_lookup(context_hash),
  primary key(resource_hash, context_hash)
  ) partition by hash(resource_hash);
create index if not exists resource_context_map_type_hash_idx on resource_context_map(context_hash);
comment on table resource_context_map is 'Map of all resource to context';

--rollback drop table if exists resource_context_map;
