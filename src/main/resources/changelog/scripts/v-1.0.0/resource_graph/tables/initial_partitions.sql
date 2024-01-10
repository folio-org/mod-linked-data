--liquibase formatted sql

--changeset dfeeney@ebsco.com:3.10_initial_partitions dbms:postgresql splitStatements:false

do $do$
  BEGIN

  perform create_rebalance_workspace();
  perform identify_scale_partitions(parent_name, 2) from table_order order by id;
  perform add_graph_scale_partitions(parent_name) from table_order order by id;
  perform drop_rebalance_workspace();

  END
$do$;
