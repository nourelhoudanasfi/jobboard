-- =====================================================================
--  candidat-service - MySQL database: candidat_db
--  Run with:  mysql -u root -p < candidat_db.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS candidat_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE candidat_db;

DROP TABLE IF EXISTS candidate;
DROP TABLE IF EXISTS address;

CREATE TABLE address (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    street       VARCHAR(150) NOT NULL,
    house_number VARCHAR(10)  NOT NULL,
    zip_code     VARCHAR(10)  NOT NULL,
    CONSTRAINT pk_address PRIMARY KEY (id)
) ENGINE = InnoDB;

-- one candidate -> at most one address (1..1)
CREATE TABLE candidate (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    firstname  VARCHAR(50)  NOT NULL,
    lastname   VARCHAR(50)  NOT NULL,
    email      VARCHAR(150) NOT NULL,
    address_id BIGINT       NULL,
    CONSTRAINT pk_candidate PRIMARY KEY (id),
    CONSTRAINT uk_candidate_email UNIQUE (email),
    CONSTRAINT uk_candidate_address UNIQUE (address_id),
    CONSTRAINT fk_candidate_address FOREIGN KEY (address_id)
        REFERENCES address (id)
        ON DELETE SET NULL
) ENGINE = InnoDB;

-- Sample data (same ids as the monolith, so the candidature service can reuse them)
INSERT INTO address (street, house_number, zip_code) VALUES
  ('Avenue Habib Bourguiba', '12', '1001'),
  ('Rue de Marseille',       '5',  '1000'),
  ('Rue du Lac Leman',       '27', '1053');

INSERT INTO candidate (firstname, lastname, email, address_id) VALUES
  ('Amine', 'Ben Salah', 'amine.bensalah@example.com', 1),
  ('Sarra', 'Trabelsi',  'sarra.trabelsi@example.com', 2),
  ('Yassine', 'Gharbi',  'yassine.gharbi@example.com', 3),
  ('Lina', 'Mansour',    'lina.mansour@example.com',   NULL);
