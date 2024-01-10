--liquibase formatted sql

--changeset dfeeney@ebsco.com:3.8_generator_manifest dbms:postgresql

create table if not exists generator_manifest (
  input_hash bigint not null,
  concrete_hash bigint not null,
  generator_hash bigint not null references resources(resource_hash) on delete cascade,
  conceptual_hash bigint not null,
  created_event_id bigint null,
  updated_event_id bigint null,
  active_event_id bigint null,
  primary key (input_hash, concrete_hash, generator_hash)
  ) partition by hash(generator_hash);
comment on table generator_manifest is 'Manifest of resources and their sources sufficient to generate a full subgraph of resources';
comment on column generator_manifest.concrete_hash is 'A hash representing the source entity that generated the resource';
comment on column generator_manifest.conceptual_hash is 'A hash representing the generating source entity in the abstract';
comment on column generator_manifest.generator_hash is 'A hash representing a resource associated to this source entity';
comment on column generator_manifest.input_hash is 'A hash representing the input source';
comment on column generator_manifest.created_event_id is 'The event that created the edge';
comment on column generator_manifest.updated_event_id is 'The latest event that updated the edge';
comment on column generator_manifest.active_event_id is 'The event that indicates if the edge is active';

create index if not exists generator_manifest_conceptual_hash on generator_manifest(conceptual_hash);
create index if not exists generator_manifest_concrete_hash on generator_manifest(concrete_hash);
create index if not exists generator_manifest_generator_hash on generator_manifest(generator_hash);


--rollback drop table generator_manifest;

