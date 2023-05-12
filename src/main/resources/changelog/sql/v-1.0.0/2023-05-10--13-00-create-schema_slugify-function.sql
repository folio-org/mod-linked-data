CREATE OR REPLACE FUNCTION schema_slugify(name text) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $$
  select substring(trim(regexp_replace(lower(name), '[^a-z0-9_]+', '_', 'gi'), '_'), 1, 53);
$$;
