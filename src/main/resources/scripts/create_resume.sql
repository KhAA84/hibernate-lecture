create table resume(
     resume_id SERIAL PRIMARY KEY,
     user_id INTEGER,
     description VARCHAR(512),
     active BOOLEAN
);
