--liquibase formatted sql

--changeset create_preferred_profile_settings_table dbms:postgresql

create table if not exists preferred_profile_settings
(
  user_id              uuid      not null,
  profile_id           smallint  not null,
  profile_settings_id  int       not null,
  primary key (user_id, profile_id),
  foreign key (profile_id) references profiles (id) on delete cascade,
  foreign key (profile_settings_id) references profile_settings (id) on delete cascade
);

--rollback drop table preferred_profile_settings;
