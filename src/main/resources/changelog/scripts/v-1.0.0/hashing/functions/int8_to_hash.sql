--liquibase formatted sql

--changeset dfeeney@ebsco.com:1.2 dbms:postgresql

CREATE OR REPLACE FUNCTION int8_to_hash(bigint)
  RETURNS text
AS $$
select replace(replace((trim( trailing '=' from
                              encode(int8send($1), 'base64'))),
                       '/', '_'),
               '+', '-')
$$
  LANGUAGE SQL STRICT IMMUTABLE parallel safe;

--rollback drop function if exists int8_to_hash(bigint);
