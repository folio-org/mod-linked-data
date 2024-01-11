--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.15_get_configuration_setting_function dbms:postgresql splitStatements:false

do $do$
  BEGIN
execute format($format$

    create or replace function %1$I.get_configuration_setting(key text, default_value text) returns text as
    $get_configuration_setting$
      -- Arguments can't be changed after signature change.
      -- `key` should be `in_group_name`
      -- `default_value` should be `in_setting_name`
      -- they cannot be changed because the function is used in web views, so can't be dropped.
    select coalesce((select setting_value from %1$I.graph_settings where setting_id=c.id), c.default_value)
    from %1$I.configurable_settings c
    where c.group_name=$1
      and c.setting_name=$2;

    $get_configuration_setting$
      language sql
      stable
      strict;
    comment on function %1$I.get_configuration_setting(text, text) is 'Retrieve the value for a configuration parameter for a graph by group and setting name';

  $format$, CURRENT_SCHEMA);
  END;
$do$;

--rollback drop function if exists  get_configuration_setting(text, text);
