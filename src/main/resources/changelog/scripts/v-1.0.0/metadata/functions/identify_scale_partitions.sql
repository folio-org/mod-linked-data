--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.9_identify_scale_partitions_function dbms:postgresql splitStatements:false
do $do$
  BEGIN

  execute format($format$

    create or replace function identify_scale_partitions(in_parent_table_name text, scaling_exponent int)
    returns boolean
    as $$
    DECLARE
      active_graph boolean;
      partition_count int;
      graph_schema text;
      parent_table regclass;
    BEGIN
      if scaling_exponent < 0 or scaling_exponent > 10 then
        raise exception 'Scaling exponent %% is out of range of 0-10 (1-1024 partitions)', scaling_exponent;
      end if;

      graph_schema := %1$L;

      select exists (select 1
                     from information_schema.tables
                     where table_schema=graph_schema
                       and table_name='new_tables') into active_graph;
      if not active_graph then
        raise exception 'Rebalance workspace has not been created. Invoke `%1$I.create_rebalance_workspace()`';
      end if;

      select exists (select 1
                     from information_schema.tables
                     where table_schema=graph_schema
                       and table_name=in_parent_table_name) into active_graph;
      if not active_graph then
        raise exception '%% is not a table', in_parent_table_name;
      end if;

      parent_table := format('%%I.%%I', graph_schema, in_parent_table_name)::regclass;

      select %1$I.is_partitioned(parent_table) into active_graph;
      if not active_graph then
        raise notice 'Table %% is not a partitioned table', parent_table;
        return false;
      end if;
      partition_count := power(2, scaling_exponent);
      insert into %1$I.new_tables(parent_name, parent_table, partition_schema, partition_name, modulus, remainder)
      select
        in_parent_table_name,
        parent_table,
        graph_schema,
        format('%%s_%%s_%%s',  in_parent_table_name, partition_count, x),
        partition_count,
        x
      from generate_series(0, partition_count - 1) x;
      return true;
    END
    $$
      language plpgsql
      security definer
      set search_path=%1$I,public;
    $format$, CURRENT_SCHEMA);

  END;
$do$;

comment on function identify_scale_partitions is 'returns a list of partition tables for a parent table based on a scaling factor';

--rollback drop function if exists  identify_scale_partitions(text, int);
