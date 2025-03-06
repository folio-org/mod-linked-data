--liquibase formatted sql

--changeset dfeeney@ebsco.com:3.13_drop_staging_tables_function dbms:postgresql splitStatements:false
do $do$
  BEGIN

    execute format($format$
      create or replace function %1$I.drop_staging_tables() returns boolean as
      $drop_staging_tables$
      DECLARE
        partitioned_list text[] := array[
          'resources_staging',
          'resource_edges_staging',
          'resource_generator_map_staging',
          'generator_manifest_staging',
          'resource_context_map_staging',
          'resource_type_map_staging'
        ];
        partition_schema text;
        partition_table text;
        staging_table text;
        parent_table regclass;
      BEGIN
        foreach staging_table in array partitioned_list loop
          if exists(select 1 from information_schema.tables
                    where table_schema=%1$L and table_name=staging_table) then
            for partition_schema, partition_table in
              select
                 nsp.nspname, tbl.relname
              from pg_catalog.pg_inherits i
              join pg_class tbl on i.inhrelid=tbl.oid
              join pg_namespace nsp  on nsp.oid=tbl.relnamespace
              where i.inhparent = format('%%I.%%I', %1$L, staging_table)::regclass
            loop
              execute format('drop table if exists %%I.%%I', partition_schema, partition_table);
            end loop;
            execute format('drop table if exists %%I.%%I', %1$L, staging_table);
          end if;
        end loop;

        drop table if exists %1$I.context_lookup_staging;
        drop table if exists %1$I.predicate_lookup_staging;
        drop table if exists %1$I.type_lookup_staging;

        return true;
      END
      $drop_staging_tables$
      language plpgsql
      security definer
      set search_path=%1$I,public;

  $format$, CURRENT_SCHEMA);
comment on function drop_staging_tables() is 'Drop the staging tables for graph import';

  END;
$do$;

--rollback drop function if exists drop_staging_tables()
