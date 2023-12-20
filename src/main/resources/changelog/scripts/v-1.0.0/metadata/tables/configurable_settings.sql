--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.3_configurable_settings_table dbms:postgresql
create table if not exists configurable_settings
(
  id serial primary key,
  group_name text,
  setting_name text,
  default_value text,
  description text,
  unique (group_name, setting_name)
);

comment on table configurable_settings is 'Configurable settings for graphs';
comment on column configurable_settings.group_name is 'The group a setting belongs to';
comment on column configurable_settings.setting_name is 'The name of a setting in a group';
comment on column configurable_settings.default_value is 'The default textual value for a graph setting';


--rollback drop table if exists configurable_settings;
