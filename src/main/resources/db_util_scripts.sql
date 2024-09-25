CREATE OR REPLACE FUNCTION reset_all_sequences() RETURNS void AS $$
DECLARE
    seq_record record;
BEGIN
    FOR seq_record IN
        SELECT
            s.relname AS sequence_name,
            c.relname AS table_name,
            a.attname AS column_name
        FROM
            pg_class s
                JOIN pg_depend d ON d.objid = s.oid
                JOIN pg_class c ON c.oid = d.refobjid
                JOIN pg_attribute a ON (a.attrelid = c.oid AND a.attnum = d.refobjsubid)
        WHERE
            s.relkind = 'S'
          AND c.relkind = 'r'
        LOOP
            EXECUTE format('SELECT setval(%L, COALESCE((SELECT MAX(%I) FROM %I), 1))',
                           seq_record.sequence_name,
                           seq_record.column_name,
                           seq_record.table_name);
        END LOOP;
END;
$$ LANGUAGE plpgsql;

SELECT reset_all_sequences();