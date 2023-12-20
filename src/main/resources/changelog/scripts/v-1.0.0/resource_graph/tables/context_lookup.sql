--liquibase formatted sql

--changeset dfeeney@ebsco.com:3.2_context_lookup dbms:postgresql

create table if not exists context_lookup(
  context_hash bigint primary key,
  context_uri text not null -- check (hashing.hash_text(context_uri) = context_hash)
);
comment on table context_lookup is 'Lookup of context URI by hash';

--rollback drop table predicate_lookup;
