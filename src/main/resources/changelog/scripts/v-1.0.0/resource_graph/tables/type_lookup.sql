--liquibase formatted sql

--changeset dfeeney@ebsco.com:3.3_type_lookup dbms:postgresql

create table if not exists type_lookup(
   type_hash bigint not null primary key,
   type_uri text not null, -- check (hashing.hash_text(type_uri) = type_hash),
   simple_label text
);
comment on table type_lookup is 'Lookup of type URI and label by hash';

--rollback drop table type_lookup;

