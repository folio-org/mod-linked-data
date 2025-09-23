--liquibase formatted sql

--changeset delete_all_resources dbms:postgresql

delete from resources;
