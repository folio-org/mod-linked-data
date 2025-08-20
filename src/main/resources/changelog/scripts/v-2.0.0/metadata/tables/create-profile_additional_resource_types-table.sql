--liquibase formatted sql

--changeset profile_additional_resource_types dbms:postgresql

create table if not exists profile_additional_resource_types (
    profile_id smallint not null,
    type_hash bigint not null,
    constraint pk_profile_additional_resource_types primary key (profile_id, type_hash),
    constraint fk_profile_id foreign key (profile_id) references profiles(id) on delete cascade,
    constraint fk_type_hash foreign key (type_hash) references type_lookup(type_hash) on delete cascade
);


--rollback drop table if exists profile_additional_resource_types;
