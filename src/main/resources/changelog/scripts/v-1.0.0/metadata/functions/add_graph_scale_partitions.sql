--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.19_add_graph_scale_partitions_function dbms:postgresql splitStatements:false

do $do$
  BEGIN
    execute format($format$
      create or replace function %1$I.add_graph_scale_partitions(in_parent_name text)
          returns table (
              parent_name text,
              partition_schema text,
              partition_name text,
              partition_created boolean,
              partition_attached boolean
          ) as
      $$
      DECLARE
          graph_schema text;
          target record;
          parent_table regclass;
          modulus int;
          remainder int;
      BEGIN
          graph_schema := %1$L;

          for target in select nt.parent_table, nt.partition_schema, nt.partition_name, nt.modulus, nt.remainder
                    from %1$I.new_tables nt
                    where nt.parent_name=in_parent_name
          loop
              parent_name := target.parent_table::text;
              partition_schema := target.partition_schema;
              partition_name := target.partition_name;
              modulus := target.modulus;
              remainder := target.remainder;

              if exists (select 1
                         from information_schema.tables
                         where table_schema=target.partition_schema
                           and table_name=target.partition_name)
              then

                  if exists (
                      select 1 from pg_catalog.pg_inherits
                      where inhparent=target.parent_table
                          and inhrelid=format('%%I.%%I', target.partition_schema, target.partition_name)::regclass
                  ) then
                      partition_created := false;
                      partition_attached := false;
                      return next;
                  else
                      execute format(
                      $quote$
                          alter table %%1$I.%%3$I attach partition %%1$I.%%2$I for values with (modulus %%4$s, remainder %%5$s)
                      $quote$, target.partition_schema, target.partition_name, in_parent_name, target.modulus, target.remainder);
                      partition_created := false;
                      partition_attached := true;
                      return next;
                  end if;
              else
                  execute format(
                  $quote$
                      create table %%1$I.%%2$I partition of %%1$I.%%3$I for values with (modulus %%4$s, remainder %%5$s)
                  $quote$, target.partition_schema, target.partition_name, in_parent_name, target.modulus, target.remainder);
                  partition_created := true;
                  partition_attached := true;
                  return next;
              end if;
          end loop;
      END
      $$
      language plpgsql
      security definer
      set search_path=%1$s,public;

      comment on function %1$I.add_graph_scale_partitions(text) is 'Creates new partitions on a table based on pre-calculated values in a workspace';
    $format$, CURRENT_SCHEMA);
  END;
$do$;

--rollback drop function if exists add_graph_scale_partitions(text);
