DROP TABLE IF EXISTS transaction;
DROP TABLE IF EXISTS user_connections;
DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          username VARCHAR(100) UNIQUE NOT NULL,
                          email VARCHAR(255) UNIQUE NOT NULL,
                          password VARCHAR(255) NOT NULL
);

CREATE TABLE user_connections (
                                  user_id INT NOT NULL,
                                  connection_id INT NOT NULL,
                                  PRIMARY KEY (user_id, connection_id),
                                  FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
                                  FOREIGN KEY (connection_id) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE TABLE transaction (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             sender_id INT NOT NULL,
                             receiver_id INT NOT NULL,
                             description VARCHAR(255),
                             amount DECIMAL(10,2) NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (sender_id) REFERENCES app_user(id),
                             FOREIGN KEY (receiver_id) REFERENCES app_user(id)
);

-- Insertion de 10 utilisateurs
INSERT INTO app_user (username, email, password) VALUES
                                                     ('alice', 'alice@mail.com', '$2a$10$xJwL5vxZJhNpDLwFpQhQ.eGYM3U9Q6QZ7Jz8cBQd6w7tq1JkXvYbG'), -- Motdepasse1
                                                     ('bob', 'bob@mail.com', '$2a$10$yHpKjwTmZRkOqN7NGQxR.e3z3Q1WQZ7Jz8cBQd6w7tq1JkXvYbG'),     -- Motdepasse2
                                                     ('charlie', 'charlie@mail.com', '$2a$10$zKlPkjwTmZRkOqN7NGQxR.e3z3Q1WQZ7Jz8cBQd6w7tq1JkXvYbG'),
                                                     ('diana', 'diana@mail.com', '$2a$10$wMxPkjwTmZRkOqN7NGQxR.e3z3Q1WQZ7Jz8cBQd6w7tq1JkXvYbG'),
                                                     ('eve', 'eve@mail.com', '$2a$10$vNcQkjwTmZRkOqN7NGQxR.e3z3Q1WQZ7Jz8cBQd6w7tq1JkXvYbG'),
                                                     ('frank', 'frank@mail.com', '$2a$10$uBdRkjwTmZRkOqN7NGQxR.e3z3Q1WQZ7Jz8cBQd6w7tq1JkXvYbG'),
                                                     ('grace', 'grace@mail.com', '$2a$10$tAsEkjwTmZRkOqN7NGQxR.e3z3Q1WQZ7Jz8cBQd6w7tq1JkXvYbG'),
                                                     ('henry', 'henry@mail.com', '$2a$10$sZrDkjwTmZRkOqN7NGQxR.e3z3Q1WQZ7Jz8cBQd6w7tq1JkXvYbG'),
                                                     ('isabel', 'isabel@mail.com', '$2a$10$rYqCkjwTmZRkOqN7NGQxR.e3z3Q1WQZ7Jz8cBQd6w7tq1JkXvYbG'),
                                                     ('jack', 'jack@mail.com', '$2a$10$qXpBkjwTmZRkOqN7NGQxR.e3z3Q1WQZ7Jz8cBQd6w7tq1JkXvYbG');

-- Connexions entre utilisateurs (réseau social)
INSERT INTO user_connections (user_id, connection_id) VALUES
                                                          (1, 2), (1, 3), (1, 4), -- Alice est amie avec Bob, Charlie et Diana
                                                          (2, 3), (2, 5),         -- Bob est amie avec Charlie et Eve
                                                          (3, 4), (3, 6),          -- Charlie est amie avec Diana et Frank
                                                          (4, 7), (4, 8),          -- Diana est amie avec Grace et Henry
                                                          (5, 9),                  -- Eve est amie avec Isabel
                                                          (6, 10),                 -- Frank est amie avec Jack
                                                          (7, 8), (7, 9),          -- Grace est amie avec Henry et Isabel
                                                          (8, 10),                 -- Henry est amie avec Jack
                                                          (9, 10);                 -- Isabel est amie avec Jack

-- Transactions entre utilisateurs
INSERT INTO transaction (sender_id, receiver_id, description, amount, created_at) VALUES
                                                                                      (1, 2, 'Déjeuner', 15.50, '2023-01-15 12:30:00'),
                                                                                      (2, 1, 'Remboursement', 7.75, '2023-01-16 09:15:00'),
                                                                                      (1, 3, 'Cadeau anniversaire', 25.00, '2023-02-01 08:00:00'),
                                                                                      (3, 4, 'Partage loyer', 300.00, '2023-02-05 18:30:00'),
                                                                                      (4, 5, 'Concert', 45.00, '2023-02-10 20:00:00'),
                                                                                      (5, 6, 'Covoiturage', 20.00, '2023-02-15 17:45:00'),
                                                                                      (6, 7, 'Matériel informatique', 120.00, '2023-03-01 10:20:00'),
                                                                                      (7, 8, 'Cours particulier', 30.00, '2023-03-05 15:00:00'),
                                                                                      (8, 9, 'Livre d\'occasion', 12.50, '2023-03-10 11:30:00'),
(9, 10, 'Jeu vidéo', 35.00, '2023-03-15 19:00:00'),
(10, 1, 'Remboursement prêt', 200.00, '2023-04-01 14:00:00'),
(2, 4, 'Week-end', 150.00, '2023-04-05 16:45:00'),
(3, 5, 'Cinéma', 24.00, '2023-04-10 21:30:00'),
(4, 6, 'Restaurant', 42.50, '2023-04-15 13:15:00'),
(5, 7, 'Cours de musique', 40.00, '2023-05-01 17:00:00');