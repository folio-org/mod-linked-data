--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.18_validate_partition_state_function dbms:postgresql splitStatements:false

do $do$
  BEGIN

    execute format($format$
      create or replace function %1$I.validate_partition_state() returns boolean as
      $validate_partition_state$
      DECLARE
        entity_exists boolean;
      BEGIN

        select (exists (select 1
                 from information_schema.tables
                 where table_schema=%1$L
                   and table_name='existing_partitions')
                and exists (select 1
                 from information_schema.tables
                 where table_schema=%1$L
                   and table_name='new_tables')) into entity_exists;
        if not entity_exists then
          raise exception 'Rebalance workspace has not been created. Invoke `%1$I.create_rebalance_workspace()`';
        end if;

        select exists (
          select 1
          from  %1$I.new_tables n
          join  %1$I.existing_partitions r
          on r.parent_table=n.parent_table
            and r.partition_schema=n.partition_schema
            and r.partition_name=n.partition_name
        ) into entity_exists;

        return not entity_exists;
      END;
      $validate_partition_state$
      language plpgsql
      security definer
      set search_path=%1$I,public;

      comment on function %1$I.validate_partition_state() is 'Returns true if there is no overlap between existing partition tables and expected new tables';


  $format$, CURRENT_SCHEMA);
  END;
$do$;


--rollback drop function if exists  validate_partition_state();
