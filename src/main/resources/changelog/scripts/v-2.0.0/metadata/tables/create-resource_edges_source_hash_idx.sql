--liquibase formatted sql

--changeset create-resource_edges_source_hash_idx dbms:postgresql

create index if not exists resource_edges_source_hash_idx on resource_edges(source_hash);

--rollback drop index if exists resource_edges_source_hash_idx;
