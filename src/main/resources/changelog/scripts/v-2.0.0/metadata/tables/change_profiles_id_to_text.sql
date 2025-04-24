--liquibase formatted sql

--changeset askhat_abishev@epam.com:4.1_profiles_table dbms:postgresql
ALTER TABLE profiles ADD COLUMN id_text text;
UPDATE profiles SET id_text = id::text;
UPDATE profiles SET id_text = 'monograph-1.0.1' WHERE id_text = '1';
ALTER TABLE profiles DROP CONSTRAINT profiles_pkey;
ALTER TABLE profiles DROP COLUMN id;
ALTER TABLE profiles RENAME COLUMN id_text TO id;
ALTER TABLE profiles ADD PRIMARY KEY (id);
