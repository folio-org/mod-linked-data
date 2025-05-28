--liquibase formatted sql

--changeset upgrade_profiles_table_structure dbms:postgresql

-- Delete all profiles
delete from profiles;

-- Add 'name' column
alter table profiles add column name text not null;

-- Add 'resource_type' column
alter table profiles add column resource_type bigint not null;

alter table profiles
  add constraint fk_profiles_resource_type foreign key (resource_type)
    references type_lookup (type_hash)
    on delete cascade;

--rollback alter table profiles drop constraint fk_profiles_resource_type;
--rollback alter table profiles drop column resource_type;
--rollback alter table profiles drop column name;
