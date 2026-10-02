-- Hibernate creates CHECK constraints for enum columns but ddl-auto=update never updates them,
-- so new enum values (like ITEM_REMOVED) get rejected. Drop it; the Java enum still validates.
ALTER TABLE audit_entry DROP CONSTRAINT IF EXISTS audit_entry_action_type_check;
