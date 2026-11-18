ALTER TABLE comments
DROP CONSTRAINT comments_article_id_fkey;

ALTER TABLE comments
    ADD CONSTRAINT comments_article_id_fkey
        FOREIGN KEY (article_id)
            REFERENCES articles(id)
            ON DELETE CASCADE;