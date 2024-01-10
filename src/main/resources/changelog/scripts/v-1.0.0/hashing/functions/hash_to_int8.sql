--liquibase formatted sql

--changeset dfeeney@ebsco.com:1.1 dbms:postgresql
CREATE OR REPLACE FUNCTION hash_to_int8(text)
  RETURNS int8
AS $$
select trim(leading '\' from
            decode(rpad(replace(replace($1, '-', '+'), '_', '/') , 12, '='),
                   'base64')::text)::bit(64)::bigint
$$
  LANGUAGE SQL STRICT IMMUTABLE parallel safe;

comment on function hash_to_int8(text) is 'Converts the text representation of a graph hash to int8';

--rollback drop function if exists hash_to_int8(text);

