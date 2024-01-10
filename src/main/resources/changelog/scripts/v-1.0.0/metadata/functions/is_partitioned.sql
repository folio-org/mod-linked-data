--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.7_is_partitioned_function dbms:postgresql

create or replace function is_partitioned(_table regclass) returns boolean as
$is_partitioned$
select relkind='p' from pg_class where oid=_table::oid
$is_partitioned$
  language sql
  strict
  stable
  parallel safe;
comment on function is_partitioned(regclass) is 'Return whether or not a particular table is partitioned';

--rollback drop function if exists  is_partitioned(regclass);
