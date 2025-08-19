--liquibase formatted sql

--changeset upgrade_profiles_table_with_additional_type dbms:postgresql

-- Add 'additional_resource_type' column
alter table profiles add column additional_resource_type bigint NULL;

alter table profiles
  add constraint fk_profiles_additional_resource_type foreign key (additional_resource_type)
    references type_lookup (type_hash)
    on delete cascade;

--rollback alter table profiles drop constraint fk_profiles_additional_resource_type;
--rollback alter table profiles drop column additional_resource_type;
