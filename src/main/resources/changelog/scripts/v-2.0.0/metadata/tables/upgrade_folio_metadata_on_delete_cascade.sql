--liquibase formatted sql

--changeset pkjacob@ebsco.com:folio_metadata_on_delete_cascade dbms:postgresql
alter table folio_metadata
drop constraint if exists folio_metadata_resource_hash_fkey;

alter table folio_metadata
  add constraint folio_metadata_resource_hash_fkey
    foreign key (resource_hash)
      references resources(resource_hash)
      on delete cascade;

--rollback alter table folio_metadata drop constraint if exists folio_metadata_resource_hash_fkey;
