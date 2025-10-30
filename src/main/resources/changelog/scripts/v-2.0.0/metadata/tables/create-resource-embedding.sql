--liquibase formatted sql

--changeset create_resource_embeddings dbms:postgresql

CREATE TABLE resource_embeddings (
   resource_hash bigint not null primary key,
   embedding vector(768)
);

--rollback drop table if exists resource_embeddings;
