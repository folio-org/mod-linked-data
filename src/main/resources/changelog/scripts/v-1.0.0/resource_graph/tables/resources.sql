--liquibase formatted sql

--changeset dfeeney@ebsco.com:3.4_resources dbms:postgresql
CREATE TABLE if not exists resources (
  resource_hash bigint not null primary key,
  doc jsonb null,
  label text null,
  resource_uri text,
  created_event_id bigint null,
  updated_event_id bigint null,
  active_event_id bigint null,
  index_date timestamp,
  active boolean default true not null,
  created_date timestamp default current_timestamp not null,
  updated_date timestamp default current_timestamp not null,
  created_by uuid,
  updated_by uuid,
  version int default 0 not null
) partition by hash(resource_hash);

create index if not exists resources_active_idx on resources(active);

comment on table resources is 'All resources in a graph and their non-link data';
comment on column resources.resource_hash is 'The unique hash identifier for the resource';
comment on column resources.doc is 'JSON representation of literal properties for the resource';
comment on column resources.label is 'Descriptive label for the resource';
comment on column resources.created_event_id is 'The event that created the resource';
comment on column resources.updated_event_id is 'The latest event that updated the resource';
comment on column resources.active_event_id is 'The event that indicates if the resource is active';
comment on column resources.index_date is 'The date this resource was indexed to OpenSearch';
comment on column resources.active is 'The flag that indicates if the resource is active';
comment on column resources.created_date is 'Date and time when resource first added to data graph';
comment on column resources.created_by is 'UUID of user who added resource to data graph';
comment on column resources.updated_date is 'Date and time when resource last updated';
comment on column resources.updated_by is 'UUID of user who performed the last update to the resource';
comment on column resources.version is 'Version of the resource';

--rollback drop table if exists resources;
