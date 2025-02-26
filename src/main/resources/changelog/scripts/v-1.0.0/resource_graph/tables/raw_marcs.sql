--liquibase formatted sql

--changeset pkjacob@ebsco.com:3.4_raw_marcs dbms:postgresql
create table if not exists raw_marcs (
     resource_hash bigint primary key references resources(resource_hash),
     content jsonb null
  ) partition by hash(resource_hash);

comment on table raw_marcs is 'Store unmapped MARC records associated with an Instance resource. Applicable only for Instance resources';
comment on column raw_marcs.resource_hash is 'The unique hash identifier for the resource';
comment on column raw_marcs.content is 'JSON representation of MARC record';

--rollback drop table if exists raw_marcs;
