--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.5_profiles_table dbms:postgresql

create table if not exists profiles (
  id smallint primary key,
  value jsonb not null
);

--rollback drop table if exists profiles;
