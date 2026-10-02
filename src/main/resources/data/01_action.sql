-- Scavenge is its own feature now, so the Scavenge action was renamed to Forage (#149).
-- Renames the row in databases seeded before that, keeping its id. Must stay above the INSERT,
-- otherwise the INSERT adds a second Forage row first.
UPDATE action
SET name = 'Forage', type = 'Forage', effect = 'Search for food and other necessities'
WHERE name = 'Scavenge';

-- Runs on every startup, so it must be safe to repeat: only inserts actions whose name isn't there yet.
-- Uses NOT EXISTS rather than ON CONFLICT (name), because ON CONFLICT needs a unique constraint on
-- action.name, and ddl-auto=update doesn't add one to a table that already existed.
INSERT INTO action (name, type, effect, target, strength, endurance, agility, courage, intelligence, leadership, trustworthiness)
SELECT v.*
FROM (VALUES
    ('Attack', 'Attack', 'Deal damage to a threat', 'Enemy', 0.6, 0.1, 0.2, 0.1, 0.0, 0.0, 0.0),
    ('Heal', 'Heal', 'Restore health to a survivor', 'Survivor', 0.0, 0.1, 0.1, 0.0, 0.4, 0.0, 0.4),
    ('Forage', 'Forage', 'Search for food and other necessities', 'Location', 0.1, 0.1, 0.4, 0.0, 0.3, 0.0, 0.1),
    ('Build Shelter', 'Build', 'Build a safe shelter', 'Location', 0.4, 0.2, 0.1, 0.1, 0.2, 0.0, 0.0),
    ('Persuade', 'Persuade', 'Convince another person', 'Person', 0.0, 0.0, 0.1, 0.0, 0.1, 0.4, 0.4)
) AS v(name, type, effect, target, strength, endurance, agility, courage, intelligence, leadership, trustworthiness)
WHERE NOT EXISTS (SELECT 1 FROM action a WHERE a.name = v.name);

-- Items each action requires: type is 'tool' or 'weapon', name NULL means any item of that type.
-- Actions not listed (Forage, Persuade) need nothing. Same NOT EXISTS pattern, so it is safe to repeat;
-- IS NOT DISTINCT FROM so a NULL name counts as a match.
INSERT INTO action_required_item (action_id, type, name)
SELECT a.id, v.type, v.name
FROM (VALUES
    ('Attack', 'weapon', NULL),
    ('Heal', 'tool', 'First Aid Kit'),
    ('Build Shelter', 'tool', NULL)
) AS v(action_name, type, name)
JOIN action a ON a.name = v.action_name
WHERE NOT EXISTS (
    SELECT 1 FROM action_required_item r
    WHERE r.action_id = a.id AND r.type = v.type AND r.name IS NOT DISTINCT FROM v.name
);
