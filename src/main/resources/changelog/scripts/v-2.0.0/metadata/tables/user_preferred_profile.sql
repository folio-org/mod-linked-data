--liquibase formatted sql

--changeset create_user_profile_table dbms:postgresql

create table if not exists preferred_profiles
(
  user_id       uuid     NOT NULL,
  resource_type bigint   NOT NULL,
  profile_id    smallint NOT NULL,
  primary key (user_id, resource_type),
  foreign key (resource_type) references type_lookup (type_hash) on delete cascade,
  foreign key (profile_id) references profiles (id) on delete cascade
  );

comment on table preferred_profiles is 'Stores preferred profile for a user for the given resource type';
comment on column preferred_profiles.user_id is 'Identifier for the user';
comment on column preferred_profiles.resource_type is 'Reference to the resource type (type_hash from type_lookup)';
comment on column preferred_profiles.profile_id is 'Reference to the preferred profile';

--rollback drop table if exists preferred_profiles;
