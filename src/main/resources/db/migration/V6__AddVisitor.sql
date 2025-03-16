INSERT INTO users (username, password, role)
SELECT 'visitor', '96e79218965eb72c92a549dd5a330112', 'visitor'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'visitor'
);