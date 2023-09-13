DROP TABLE IF EXISTS people;

--CREATE TABLE people  (
--    person_id BIGINT IDENTITY NOT NULL PRIMARY KEY,
--    first_name VARCHAR(20),
--    last_name VARCHAR(20)
--);

CREATE TABLE people (
    person_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name varchar(25),
    last_name varchar(25)
);

-- Spring Boot runs schema-@@platform@@.sql automatically during startup. -all is the default for all platforms.