--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.10_create_rebalance_workspace_function dbms:postgresql splitStatements:false

do $do$
  BEGIN

  execute format($format$

    create or replace function %1$I.create_rebalance_workspace() returns boolean as
    $$
    BEGIN
      drop table if exists %1$I.existing_partitions;
      drop table if exists %1$I.new_tables;
      drop table if exists %1$I.table_order;

      create table %1$I.table_order(
        id serial,
        parent_name text
      );

      insert into %1$I.table_order(parent_name) values ('resources'),
                                                        ('resource_edges'),
                                                        ('resource_type_map'),
                                                        ('resource_context_map'),
                                                        ('generator_manifest'),
                                                        ('resource_generator_map');

      create table %1$I.existing_partitions (
        id serial,
        parent_name text,
        parent_table regclass,
        partitioned_table regclass,
        partition_schema text,
        partition_name text
      );

      create table %1$I.new_tables (
        id serial,
        parent_name text,
        parent_table regclass,
        partition_schema text,
        partition_name text,
        modulus int,
        remainder int
      );

      return true;
    END
    $$
    language plpgsql
    security definer
    set search_path=%1$I,public;

    comment on function %1$I.create_rebalance_workspace is 'Creates rebalancing tables, dropping them first if they exist';


  $format$, CURRENT_SCHEMA);
  END;
$do$;



--rollback drop function if exists  create_rebalance_workspace();
