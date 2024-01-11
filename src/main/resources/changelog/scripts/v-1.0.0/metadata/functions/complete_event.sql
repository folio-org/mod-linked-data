--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.13_complete_event_function dbms:postgresql splitStatements:false
do $do$
  BEGIN

    execute format($format$
      create or replace function %1$I.complete_event(in_event_id bigint, in_metadata jsonb default '{}'::jsonb) returns bigint as
      $complete_event$
      DECLARE
        has_element boolean;
      BEGIN
        select exists (select 1 from %1$I.events where id=in_event_id and completed is null) into has_element;
        if not has_element then
          raise exception 'Nonexistent or completed event ID --> %%', in_event_id;
        end if;

        update %1$I.events set completed=transaction_timestamp(), metadata=in_metadata, is_successful=true
        where id=in_event_id;

        return in_event_id;
      END
      $complete_event$
      language plpgsql;


  $format$, CURRENT_SCHEMA);
  END;
$do$;
comment on function  complete_event(bigint, jsonb) is 'Mark a running event as complete';

--rollback drop function if exists  complete_event(bigint, jsonb);
