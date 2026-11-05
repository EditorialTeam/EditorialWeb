ALTER TABLE users RENAME COLUMN display_name TO username;

ALTER TABLE users
    ALTER COLUMN username TYPE TEXT,
    ALTER COLUMN username SET NOT NULL,
    ADD COLUMN display_name TEXT;
