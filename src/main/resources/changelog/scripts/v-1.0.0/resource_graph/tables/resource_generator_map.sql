--liquibase formatted sql

--changeset dfeeney@ebsco.com:3.9_resource_generator_map dbms:postgresql

create table if not exists resource_generator_map (
  generator_hash bigint not null,
  resource_hash bigint not null references resources(resource_hash) on delete cascade,
  created_event_id bigint null,
  updated_event_id bigint null,
  active_event_id bigint null,
  primary key (generator_hash, resource_hash)
) partition by hash(generator_hash);
comment on table resource_generator_map is 'Map of all resources past and present to the generator element they originated from';
comment on column resource_generator_map.generator_hash is 'A hash representing the source entity that generated the resource';
comment on column resource_generator_map.resource_hash is 'A hash representing a resource associated to the source entity';
comment on column resource_generator_map.created_event_id is 'The event that created the edge';
comment on column resource_generator_map.updated_event_id is 'The latest event that updated the edge';
comment on column resource_generator_map.active_event_id is 'The event that indicates if the edge is active';

create index if not exists resource_generator_map_resource_hash on resource_generator_map(resource_hash);

--rollback drop table resource_generator_map;
