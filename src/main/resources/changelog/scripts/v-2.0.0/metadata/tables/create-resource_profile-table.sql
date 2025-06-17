--liquibase formatted sql

--changeset create_resource_profile_table dbms:postgresql

create table if not exists resource_profile
(
  resource_hash bigint not null,
  profile_id bigint not null,
  primary key (resource_hash),
  foreign key (resource_hash) references resources (resource_hash) on delete cascade,
  foreign key (profile_id) references profiles (id) on delete cascade
) partition by hash(resource_hash);

comment on table resource_profile is 'Associate resource with a profile';
comment on column resource_profile.resource_hash is 'Identifier for the resource (resource_hash from resources table)';
comment on column resource_profile.profile_id is 'Reference to the associated profile';

--rollback drop table if exists resource_profile;
