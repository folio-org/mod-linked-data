--liquibase formatted sql

--changeset ppullolickal:2.0.0_create_vocabularies_table dbms:postgresql
create table if not exists vocabularies (
  vocabulary_name varchar primary key,
  vocabulary_json jsonb not null
);

--rollback drop table if exists vocabularies;
