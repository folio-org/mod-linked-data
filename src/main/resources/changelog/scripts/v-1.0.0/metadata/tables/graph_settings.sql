--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.4_graph_settings_table dbms:postgresql
create table if not exists graph_settings
(
  id serial primary key,
  setting_id integer references configurable_settings(id) on delete cascade,
  setting_value text,
  unique (setting_id)
);
create index graph_settings_graph_id_setting_id_idx on graph_settings(setting_id);
comment on table graph_settings is 'Settings for a graph';
comment on column graph_settings.id is 'Primary key for a graph setting';
comment on column graph_settings.setting_id is 'The ID for a setting on a graph';
comment on column graph_settings.setting_value is 'The text value for a graph setting';

--rollback drop table if exists graph_settings;
