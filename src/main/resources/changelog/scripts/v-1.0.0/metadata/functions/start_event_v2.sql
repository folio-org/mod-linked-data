--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.23_fix_start_event_function dbms:postgresql splitStatements:false

do $do$
  BEGIN

    execute format($format$

    create or replace function %1$I.start_event(in_event_uri text) returns bigint as
    $create_event$
    DECLARE
      generated_id bigint;
      event_hash bigint;
    BEGIN
      select event_uri_hash into event_hash from %1$I.event_lookup  where event_uri=in_event_uri;
      if event_hash is null then
        raise exception 'Nonexistent event --> %%', in_event_uri;
      end if;

      insert into %1$I.events(event_uri_hash, created)
      values (event_hash, transaction_timestamp())
      returning id into generated_id;

      return generated_id;
    END
    $create_event$
    language plpgsql;

    comment on function %1$I.start_event(text) is 'Start a new event on the graph';

  $format$, CURRENT_SCHEMA);
  END;
$do$;


--rollback drop function if exists  start_event(text);
