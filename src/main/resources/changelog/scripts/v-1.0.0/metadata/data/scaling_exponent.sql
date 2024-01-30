--liquibase formatted sql

--changeset dfeeney@ebsco.com:2.24_set_default_scaling_exponent dbms:postgresql splitStatements:false

select set_configuration_setting('graph', 'scaling_exponent', 2::text);
