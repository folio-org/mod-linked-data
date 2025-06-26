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

create table if not exists resource_profile_4_0 partition of resource_profile for values with (modulus 4, remainder 0);
create table if not exists resource_profile_4_1 partition of resource_profile for values with (modulus 4, remainder 1);
create table if not exists resource_profile_4_2 partition of resource_profile for values with (modulus 4, remainder 2);
create table if not exists resource_profile_4_3 partition of resource_profile for values with (modulus 4, remainder 3);

comment on table resource_profile is 'Associate resource with a profile';
comment on column resource_profile.resource_hash is 'Identifier for the resource (resource_hash from resources table)';
comment on column resource_profile.profile_id is 'Reference to the associated profile';

--rollback drop table if exists resource_profile;
