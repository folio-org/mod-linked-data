--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.8_find_existing_partitions_function dbms:postgresql splitStatements:false

do $do$
  BEGIN
execute format($format$

create or replace function %1$I.find_existing_partitions(in_parent_table_name text)
    returns boolean
as $$
DECLARE
    active_graph boolean;
    graph_schema text;
    parent_table regclass;
BEGIN

  graph_schema := %1$L;

  select exists (select 1
                 from information_schema.tables
                 where table_schema=graph_schema
                   and table_name='existing_partitions') into active_graph;
  if not active_graph then
    raise exception 'Rebalance workspace has not been created. Invoke `%1$I.create_rebalance_workspace()`';
  end if;

  select exists (select 1
                   from information_schema.tables
                   where table_schema=graph_schema
                     and table_name=in_parent_table_name) into active_graph;
  if not active_graph then
      raise exception '%% is not a table in graph %%', in_parent_table_name, graph_name;
  end if;

  parent_table = format('%%I.%%I', graph_schema, in_parent_table_name)::regclass;

  select %1$I.is_partitioned(parent_table) into active_graph;
  if not active_graph then
      raise notice 'Table %% is not a partitioned table', parent_table;
      return false;
  end if;

  insert into %1$I.existing_partitions(parent_name, parent_table, partitioned_table, partition_schema, partition_name)
         select
           in_parent_table_name,
           parent_table,
           inhrelid::regclass as partitioned_table,
           %1$L,
           inhrelid::regclass::text
       from pg_catalog.pg_inherits
       where inhparent = parent_table;

  return true;
END
$$
language plpgsql
security definer
set search_path=%1$s,public;

comment on function %1$I.find_existing_partitions is 'returns a list of existing partition tables for a parent table';


$format$, CURRENT_SCHEMA);
END;
$do$;

--rollback drop function if exists  find_existing_partitions(text);
