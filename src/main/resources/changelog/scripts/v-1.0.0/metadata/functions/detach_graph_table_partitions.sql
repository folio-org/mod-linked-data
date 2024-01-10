--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.18_detach_graph_table_partitions_function dbms:postgresql splitStatements:false

do $do$
  BEGIN
    execute format($format$
      create or replace function %1$I.detach_graph_table_partitions(in_parent_name text)
          returns table (
              parent_name text,
              partition_name text,
              partition_exists boolean,
              partition_detached boolean
          ) as
      $$
      DECLARE
          graph_schema text;
          entity_exists boolean;
          tables record;
      BEGIN
          graph_schema := %1$L;

          for tables in select parent_table, partitioned_table
                  from %1$I.existing_partitions ep
                  where ep.parent_name=in_parent_name

          loop
              partition_name := tables.partitioned_table::text;
              parent_name := tables.parent_table::text;
              select exists (
                  select 1 from pg_catalog.pg_inherits
                  where inhparent=tables.parent_table
                      and inhrelid=tables.partitioned_table
              ) into entity_exists;
              if not entity_exists then
                  partition_detached := false;
                  partition_exists := true;
                  return next;
              else
                  execute format(
                  $quote$
                      alter table %%s detach partition %%s
                  $quote$, tables.parent_table, tables.partitioned_table);
                  partition_exists := true;
                  partition_detached := true;
                  return next;
              end if;
          end loop;
      END
      $$
      language plpgsql
      security definer
      set search_path=%1$I,public;

      comment on function %1$I.detach_graph_table_partitions(text) is 'Detaches partitions of the provided table name on a graph. Returns whether or not each expected partition exists and was detached';
    $format$, CURRENT_SCHEMA);
END;
$do$;

--rollback drop function if exists  detach_graph_table_partitions(text);
