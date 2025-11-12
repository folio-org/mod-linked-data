--liquibase formatted sql

--changeset create-import_event_result-tables dbms:postgresql

create table if not exists import_event_result (
    event_ts bigint not null,
    job_id bigint not null,
    resources_count int not null,
    created_count int not null,
    updated_count int not null,
    failed_count int not null,
    constraint pk_import_event_result primary key (event_ts)
);

create sequence if not exists import_event_failed_resource_seq start 1 increment by 1;

create table if not exists import_event_failed_resource (
    id bigint not null,
    import_event_result_id bigint not null,
    raw_resource text not null,
    constraint pk_import_event_failed_resource primary key (id),
    constraint fk_import_event_failed_resource_to_import_event_result foreign key (import_event_result_id) references import_event_result(event_ts) on delete cascade
);

--rollback drop table if exists import_event_result;
--rollback drop sequence if exists import_event_failed_resource_seq;
--rollback drop table if exists import_event_failed_resource;
