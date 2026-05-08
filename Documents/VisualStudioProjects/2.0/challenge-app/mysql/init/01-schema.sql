CREATE DATABASE IF NOT EXISTS challenge_db;
USE challenge_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS challenges (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    goal_amount DECIMAL(10,2) NOT NULL,
    current_amount DECIMAL(10,2) DEFAULT 0,
    creator_id INT NOT NULL,
    status ENUM('active', 'completed') DEFAULT 'active',
    video_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS donations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    challenge_id INT NOT NULL,
    donor_name VARCHAR(100) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE
);

INSERT INTO users (username, email, password) VALUES
('demo_user', 'demo@example.com', '1234'),
('challenger99', 'challenger@example.com', '1234');

INSERT INTO challenges (title, description, goal_amount, current_amount, creator_id, status, video_url) VALUES
('I will learn to play guitar in 30 days', 'I will practice guitar every day for 30 days and record my progress. Help me buy a guitar to start this journey!', 500.00, 320.00, 1, 'active', NULL),
('Marathon of 42km in 3 months', 'I will train and complete a full marathon (42km) in 3 months. Every euro helps me buy proper running shoes and equipment!', 300.00, 450.00, 2, 'completed', 'https://www.youtube.com/embed/dQw4w9WgXcQ'),
('Read 20 books in 6 months', 'I commit to reading 20 books in 6 months and writing a review for each one. Help me build my home library!', 200.00, 150.00, 1, 'active', NULL);

INSERT INTO donations (challenge_id, donor_name, amount) VALUES
(1, 'Alice', 50.00),
(1, 'Bob', 100.00),
(1, 'Charlie', 170.00),
(2, 'Diana', 100.00),
(2, 'Eve', 200.00),
(2, 'Frank', 150.00),
(3, 'Grace', 75.00),
(3, 'Heidi', 75.00);
