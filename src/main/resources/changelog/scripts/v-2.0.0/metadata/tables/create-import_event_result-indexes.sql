--liquibase formatted sql

--changeset create-import_event_result-indexes dbms:postgresql

create index if not exists import_event_result_id_idx on import_event_result(event_ts);
create index if not exists import_event_failed_resource_id_idx on import_event_failed_resource(id);

--rollback drop index if exists import_event_result_id_idx;
--rollback drop index if exists import_event_failed_resource_id_idx;
