--liquibase formatted sql

--changeset upgrade_profile_settings_table dbms:postgresql

-- Create a sequence for the primary ID
create sequence profile_settings_id_seq
  start with 1;

-- Add new primary key column
alter table profile_settings
  add column id int not null default nextval('profile_settings_id_seq');

-- Remove previous primary key
alter table profile_settings
  drop constraint profile_settings_pkey;

-- Add primary key constraint
alter table profile_settings
  add constraint profile_settings_pkey primary key (id);

-- Recreate old PK index, still relevant
create index if not exists profile_settings_user_profile
  on profile_settings(user_id, profile_id);

-- Add new name column
alter table profile_settings
  add column name text;

-- Fill in placeholder value
update profile_settings
  set name = '(unnamed)';

-- Set name to not null, required field
alter table profile_settings
  alter column name set not null;

--rollback alter table profile_settings drop column name;
--rollback drop index profile_settings_user_profile;
--rollback alter table profile_settings drop constraint profile_settings_id_pk;
--rollback alter table profile_settings drop column id;
--rollback alter table profile_settings add constraint profile_settings_pkey primary key (user_id, profile_id);
