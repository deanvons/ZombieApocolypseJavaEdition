INSERT INTO action (name, type, effect, target, strength, endurance, agility, courage, intelligence, leadership, trustworthiness)
VALUES
    ('Attack', 'Attack', 'Deal damage to a threat', 'Enemy', 0.6, 0.1, 0.2, 0.1, 0.0, 0.0, 0.0),
    ('Heal', 'Heal', 'Restore health to a survivor', 'Survivor', 0.0, 0.1, 0.1, 0.0, 0.4, 0.0, 0.4),
    ('Scavenge', 'Scavenge', 'Find useful supplies', 'Location', 0.1, 0.1, 0.4, 0.0, 0.3, 0.0, 0.1),
    ('Build Shelter', 'Build', 'Build a safe shelter', 'Location', 0.4, 0.2, 0.1, 0.1, 0.2, 0.0, 0.0),
    ('Persuade', 'Persuade', 'Convince another person', 'Person', 0.0, 0.0, 0.1, 0.0, 0.1, 0.4, 0.4)
ON CONFLICT (name) DO NOTHING;
