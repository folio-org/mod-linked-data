--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.1_event_lookup_table dbms:postgresql
create table if not exists event_lookup(
  event_uri_hash bigint primary key,
  event_uri text not null  -- check (hashing.hash_text(event_uri) = event_uri_hash)
);

comment on table event_lookup is 'Lookup for event URIs by hash';

--rollback drop table if exists event_lookup;
