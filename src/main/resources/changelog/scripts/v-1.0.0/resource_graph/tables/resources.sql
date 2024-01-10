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
  inventory_id uuid,
  srs_id uuid,
  index_date timestamp
) partition by hash(resource_hash);

comment on table resources is 'All resources in a graph and their non-link data';
comment on column resources.resource_hash is 'The unique hash identifier for the resource';
comment on column resources.doc is 'JSON representation of literal properties for the resource';
comment on column resources.label is 'Descriptive label for the resource';
comment on column resources.created_event_id is 'The event that created the resource';
comment on column resources.updated_event_id is 'The latest event that updated the resource';
comment on column resources.active_event_id is 'The event that indicates if the resource is active';
comment on column resources.srs_id is 'The ID of this resource in the SRS application';
comment on column resources.inventory_id is 'The ID of this resource in the inventory application';
comment on column resources.index_date is 'The date this resource was indexed to OpenSearch';

--rollback drop table if exists resources;
