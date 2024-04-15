ALTER TABLE users
ADD CONSTRAINT user_unique_email UNIQUE (email);