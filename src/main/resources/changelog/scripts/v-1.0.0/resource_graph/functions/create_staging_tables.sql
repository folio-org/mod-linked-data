--liquibase formatted sql

--changeset dfeeney@ebsco.com:3.12_create_staging_tables_function dbms:postgresql splitStatements:false
do $do$
  BEGIN

    execute format($format$
      create or replace function %1$I.create_staging_tables() returns boolean as
      $create_staging_tables$
      DECLARE
        partitioned_list text[] := array[
          'resources_staging',
          'resource_edges_staging',
          'resource_generator_map_staging',
          'generator_manifest_staging',
          'resource_context_map_staging',
          'resource_type_map_staging'
        ];
        scaling_exponent integer;
        partition_count integer;
        staging_table text;
      BEGIN
        scaling_exponent := %1$I.get_configuration_setting('graph', 'scaling_exponent')::int;
        if scaling_exponent < 0 then
            scaling_exponent := 2;
        end if;

        partition_count := power(2, scaling_exponent);

        perform %1$I.drop_staging_tables();

        create unlogged table %1$I.resources_staging(
          resource_hash bigint,
          label text,
          doc jsonb
        ) partition by hash(resource_hash);

        create unlogged table %1$I.resource_edges_staging(
          source_hash bigint,
          target_hash bigint,
          predicate_hash bigint
        ) partition by hash(source_hash);

        create unlogged table %1$I.resource_generator_map_staging(
          generator_hash bigint,
          resource_hash bigint
        ) partition by hash(generator_hash);

        create unlogged table %1$I.generator_manifest_staging(
          input_hash bigint,
          concrete_hash bigint,
          generator_hash bigint,
          conceptual_hash bigint
        ) partition by hash(generator_hash);

        create unlogged table %1$I.resource_context_map_staging(
          resource_hash bigint,
          context_hash bigint
        ) partition by hash(resource_hash);

        create unlogged table %1$I.resource_type_map_staging(
          resource_hash bigint,
          type_hash bigint
        ) partition by hash(resource_hash);

        foreach staging_table in array partitioned_list loop
          for loop_count in 0..(partition_count - 1) loop
              execute format($partition_create$
                  create unlogged table if not exists %1$I.%%1$s_%%2$s_%%3$s partition of %1$I.%%1$I for values with (modulus %%2$s, remainder %%3$s);
              $partition_create$, staging_table, partition_count, loop_count);
          end loop;
        end loop;

        create unlogged table %1$I.context_lookup_staging (
          context_hash bigint,
          context_uri text
        );


        create unlogged table %1$I.predicate_lookup_staging (
          predicate_hash bigint,
          predicate text
        );

        create unlogged table %1$I.type_lookup_staging (
          type_hash bigint,
          type_uri text,
          simple_label text
        );

        return true;
      END
      $create_staging_tables$
      language plpgsql
      security definer
      set search_path=%1$I,public;

  $format$, CURRENT_SCHEMA);
  END;
$do$;
comment on function  create_staging_tables() is 'Create the staging tables for graph import';

--rollback drop function if exists create_staging_tables()
