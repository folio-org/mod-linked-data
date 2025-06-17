--liquibase formatted sql

--changeset pkjacob@ebsco.com:initial_partitions dbms:postgresql splitStatements:false

do $do$
BEGIN

  perform create_rebalance_workspace();
  insert into table_order(parent_name) values ('resource_profile');
  perform identify_scale_partitions(parent_name, 2) from table_order order by id;
  perform add_graph_scale_partitions(parent_name) from table_order order by id;
  perform drop_rebalance_workspace();

END
$do$;
