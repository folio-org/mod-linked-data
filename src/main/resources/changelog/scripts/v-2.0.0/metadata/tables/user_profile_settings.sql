--liquibase formatted sql

--changeset create_user_profile_settings_table dbms:postgresql

create table if not exists profile_settings
(
  user_id     uuid      NOT NULL,
  profile_id  smallint  NOT NULL,
  settings    jsonb     NOT NULL,
  primary key (user_id, profile_id),
  foreign key (profile_id) references profiles (id) on delete cascade
);

comment on table profile_settings is 'Stores workspace profile settings for a user for a profile';
comment on column profile_settings.user_id is 'Identifier for the user';
comment on column profile_settings.profile_id is 'Reference to the preferred profile';
comment on column profile_settings.settings is 'Workspace profile settings object for customizing workspace appearance';

--rollback drop table if exists user_profile_settings