--changeset pkjacob@ebsco.com:3.0_resource_source_type dbms:postgresql splitStatements:false

CREATE TYPE resource_source AS ENUM ('LINKED_DATA', 'MARC');

--rollback drop type if exists resource_source
