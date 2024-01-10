--liquibase formatted sql

--changeset dfeeney@ebsco.com:3.1_predicate_lookup dbms:postgresql

create table if not exists predicate_lookup(
  predicate_hash bigint not null primary key,
  predicate text not null -- check (hashing.hash_text(predicate) = predicate_hash)
);
comment on table predicate_lookup is 'Lookup of predicate URI by hash';

--rollback drop table predicate_lookup;
