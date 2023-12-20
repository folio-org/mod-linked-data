--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.11_drop_rebalance_workspace_function dbms:postgresql splitStatements:false

do $do$
  BEGIN

    execute format($format$


    create or replace function %1$I.drop_rebalance_workspace() returns boolean as
    $$
    DECLARE
      schema_created boolean;
      schema_name text;
    BEGIN
      drop table if exists %1$I.existing_partitions;
      drop table if exists %1$I.new_tables;
      drop table if exists %1$I.table_order;
      return true;
    END
    $$
    language plpgsql
    security definer
    set search_path=%1$I,public;

    comment on function %1$I.drop_rebalance_workspace is 'Drops rebalancing tables if they exist';

  $format$, CURRENT_SCHEMA);
  END;
$do$;

--rollback drop function if exists  drop_rebalance_workspace();
