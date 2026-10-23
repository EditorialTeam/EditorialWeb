ALTER TABLE users
DROP CONSTRAINT IF EXISTS chk_users_role;

ALTER TABLE users
    DROP COLUMN email,
    DROP COLUMN password_hash,
    DROP COLUMN username,
    DROP COLUMN role;


ALTER TABLE users
    ADD COLUMN account_id BIGINT NOT NULL,
    ADD COLUMN display_name VARCHAR(100);

ALTER TABLE users
    ADD CONSTRAINT uq_users_account_id UNIQUE (account_id);