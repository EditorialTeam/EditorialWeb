ALTER TABLE reactions
DROP CONSTRAINT reactions_article_id_fkey;

ALTER TABLE comments
    ADD CONSTRAINT reactions_article_id_fkey
        FOREIGN KEY (article_id)
            REFERENCES articles(id)
            ON DELETE CASCADE;