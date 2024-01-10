--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.14_fail_event_function dbms:postgresql splitStatements:false

do $do$
  BEGIN

    execute format($format$

      create or replace function %1$I.fail_event(in_event_id bigint, in_metadata jsonb default '{}'::jsonb) returns bigint as
      $fail_event$
      DECLARE
          has_element boolean;
          generated_id integer;
      BEGIN
          select exists (select 1 from %1$I.events where id=in_event_id and completed is null) into has_element;
          if not has_element then
            raise exception 'Nonexistent or completed event ID --> %%', in_event_id;
          end if;

          update %1$I.events set completed=transaction_timestamp(), metadata=in_metadata, is_successful=false
          where id=in_event_id;

          return in_event_id;
       END
      $fail_event$
      language plpgsql;

      comment on function %1$I.fail_event(bigint, jsonb) is 'Mark a running event as failed on a graph';

  $format$, CURRENT_SCHEMA);
  END;
$do$;


--rollback drop function if exists  fail_event(text);
