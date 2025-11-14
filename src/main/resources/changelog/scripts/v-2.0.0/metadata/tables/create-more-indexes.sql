--liquibase formatted sql

--changeset create-more-indexes dbms:postgresql

create index if not exists import_event_result_id_idx on import_event_result(event_ts);
create index if not exists import_event_failed_resource_id_idx on import_event_failed_resource(id);
create index if not exists resource_edges_source_hash_idx on resource_edges(source_hash);
create index if not exists resources_resource_hash_idx on resources(resource_hash);
create index if not exists type_lookup_type_hash_idx on type_lookup(type_hash);
create index if not exists predicate_lookup_predicate_hash_idx on predicate_lookup(predicate_hash);

--rollback drop index if exists import_event_result_id_idx;
--rollback drop index if exists import_event_failed_resource_id_idx;
--rollback drop index if exists resource_edges_source_hash_idx;
--rollback drop index if exists resources_resource_hash_idx;
--rollback drop index if exists type_lookup_type_hash_idx;
--rollback drop index if exists predicate_lookup_predicate_hash_idx;
