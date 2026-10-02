INSERT INTO camp (id, name)
VALUES ('shared', 'Shared Camp')
ON CONFLICT (id) DO NOTHING;