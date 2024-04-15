CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    email VARCHAR NOT NULL,
    birth_date VARCHAR NOT NULL,
    password VARCHAR NOT NULL
)