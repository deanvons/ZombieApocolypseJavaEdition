INSERT INTO survivor (name, type, strength, endurance, agility, courage, intelligence, leadership, trustworthiness)
VALUES
    ('John', 'CAREGIVER', 2, 6, 3, 5, 8, 5, 8),
    ('Jessica', 'HERO', 9, 6, 5, 8, 3, 6, 2),
    ('Rick', 'OUTLAW', 7, 6, 9, 6, 5, 3, 1)
ON CONFLICT (name) DO NOTHING;

-- survivor_skills has no unique constraint, so skip survivors that already have skills.
INSERT INTO survivor_skills (survivor_id, skills)
SELECT s.id, v.skill
FROM (VALUES
    ('John', 'FieldMedicine'),
    ('John', 'PsychologicalSupport'),
    ('John', 'Cooking'),
    ('Jessica', 'Marksman'),
    ('Jessica', 'HeavyWeapons'),
    ('Jessica', 'BluntWeapons'),
    ('Rick', 'ImprovisedCombat'),
    ('Rick', 'StealthCombat'),
    ('Rick', 'Intimidation'),
    ('Rick', 'TrapSetting')
) AS v(name, skill)
JOIN survivor s ON s.name = v.name
WHERE NOT EXISTS (SELECT 1 FROM survivor_skills ss WHERE ss.survivor_id = s.id);
