--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.2_events_table dbms:postgresql
create table if not exists events(
  id bigserial primary key,
  event_uri_hash bigint references event_lookup(event_uri_hash) on delete cascade,
  created timestamp with time zone DEFAULT transaction_timestamp() NOT NULL,
  completed timestamp with time zone default null,
  is_successful boolean default false,
  metadata jsonb not null default '{}'::jsonb
);
comment on table events is 'Tracks events on the graph';
comment on column events.id is 'Serial ID for an event';
comment on column events.event_uri_hash is 'The URI describing the event';
comment on column events.created is 'The date and time the event started';
comment on column events.completed is 'The date and time the event was completed';
comment on column events.is_successful is 'Whether or not the event was completed successfully';
comment on column events.metadata is 'Freeform JSON metadata related to the event';

--rollback drop table if exists events;
