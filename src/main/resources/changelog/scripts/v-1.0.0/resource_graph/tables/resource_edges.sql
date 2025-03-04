--liquibase formatted sql

--changeset dfeeney@ebsco.com:3.7_resource_edges dbms:postgresql
create table if not exists resource_edges (
  source_hash bigint not null,
  target_hash bigint not null,
  predicate_hash bigint not null,
  created_event_id bigint null,
  updated_event_id bigint null,
  active_event_id bigint null,
  primary key (source_hash, target_hash, predicate_hash),
  foreign key (source_hash) references resources(resource_hash) on delete cascade,
  foreign key (target_hash) references resources(resource_hash) on delete cascade,
  foreign key (predicate_hash) references predicate_lookup(predicate_hash) on delete cascade
) partition by hash(source_hash);

create index if not exists resource_edges_target_hash_idx on resource_edges(target_hash);
create index if not exists resource_edges_predicate_hash_idx on resource_edges(predicate_hash);

comment on table resource_edges is 'Edges between resource nodes';
comment on column resource_edges.source_hash is 'The source resource for the edge';
comment on column resource_edges.target_hash is 'The target resource for the edge';
comment on column resource_edges.predicate_hash is 'The lookup hash for the edge predicate';
comment on column resource_edges.created_event_id is 'The event that created the edge';
comment on column resource_edges.updated_event_id is 'The latest event that updated the edge';
comment on column resource_edges.active_event_id is 'The event that indicates if the edge is active';

--rollback drop table if exists resource_edges;
