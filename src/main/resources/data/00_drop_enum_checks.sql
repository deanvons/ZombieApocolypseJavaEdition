-- Hibernate creates CHECK constraints for enum columns but ddl-auto=update never updates them,
-- so new enum values (like ITEM_REMOVED) get rejected. Drop it; the Java enum still validates.
ALTER TABLE audit_entry DROP CONSTRAINT IF EXISTS audit_entry_action_type_check;
-- Same problem for survivor types: LEADER and SWEDISH were rejected, and create() reported it as a taken name
ALTER TABLE survivor DROP CONSTRAINT IF EXISTS survivor_type_check;
-- Not failing yet, but would as soon as a new ActionType or Skill is added
ALTER TABLE action DROP CONSTRAINT IF EXISTS action_type_check;
ALTER TABLE survivor_skills DROP CONSTRAINT IF EXISTS survivor_skills_skills_check;
