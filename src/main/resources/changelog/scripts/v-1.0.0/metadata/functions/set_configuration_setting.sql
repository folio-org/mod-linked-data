--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.16_set_configuration_setting_function dbms:postgresql splitStatements:false
do $do$
  BEGIN

    execute format($format$
 create or replace function %1$I.set_configuration_setting(in_group_name text, in_setting_name text, in_setting_value text) returns boolean as
  $set_configuration_setting$
      DECLARE
          target_setting_id integer;
          event_id bigint;
          stack text;
      BEGIN
        select id into target_setting_id from %1$I.configurable_settings
            where group_name=in_group_name and setting_name=in_setting_name;
        if target_setting_id is null then
            raise exception 'Nonexistent setting --> %%/%%', in_group_name, in_setting_name;
        end if;

        event_id := %1$I.start_event('http://folio.org/ext/event/set_configuration');

        BEGIN
            if in_setting_value is null then
                delete from %1$I.graph_settings where setting_id=target_setting_id;
                return true;
            end if;

            insert into %1$I.graph_settings (setting_id, setting_value)
                values(target_setting_id, in_setting_value)
            on conflict(setting_id)
            do update set setting_value=EXCLUDED.setting_value;
            perform %1$I.complete_event(event_id,
                jsonb_build_object('group_name', in_group_name, 'setting_value', in_setting_value));
        EXCEPTION when others then
            get diagnostics stack = PG_CONTEXT;
            perform %1$I.fail_event(event_id, jsonb_build_object('message', stack,
                                    'group_name', in_group_name,
                                    'setting_value', in_setting_value));
            return false;
        END;
        return true;
      END
  $set_configuration_setting$
  language plpgsql
  volatile;

  comment on function %1$I.set_configuration_setting(text, text, text) is 'Set a configuration parameter on a graph';

    $format$, CURRENT_SCHEMA);
  END;
$do$;

--rollback drop function if exists  set_configuration_setting(bigint, jsonb);
