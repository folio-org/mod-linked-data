alter table resources
  add column created_date timestamp default current_timestamp not null,
  add column updated_date timestamp default current_timestamp not null,
  add column created_by uuid,
  add column updated_by uuid,
  add column version int default 0 not null;

comment on column resources.created_date is 'Date and time when resource first added to data graph';
comment on column resources.created_by is 'UUID of user who added resource to data graph';
comment on column resources.updated_date is 'Date and time when resource last updated';
comment on column resources.updated_by is 'UUID of user who performed the last update to the resource';
comment on column resources.version is 'Version of the resource';
