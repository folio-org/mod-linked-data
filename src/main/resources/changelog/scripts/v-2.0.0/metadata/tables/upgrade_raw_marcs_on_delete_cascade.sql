--liquibase formatted sql

--changeset pkjacob@ebsco.com:3.6_raw_marcs_on_delete_cascade dbms:postgresql
alter table raw_marcs
drop constraint if exists raw_marcs_resource_hash_fkey;

alter table raw_marcs
  add constraint raw_marcs_resource_hash_fkey
    foreign key (resource_hash)
      references resources(resource_hash)
      on delete cascade;

--rollback alter table raw_marcs drop constraint if exists raw_marcs_resource_hash_fkey;
