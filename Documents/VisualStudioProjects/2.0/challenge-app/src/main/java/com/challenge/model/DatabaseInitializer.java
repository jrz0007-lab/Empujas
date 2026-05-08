package com.challenge.model;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.Statement;

@WebListener
public class DatabaseInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try (Connection conn = ConexionBD.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL, " +
                "email VARCHAR(255) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS challenges (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "description TEXT NOT NULL, " +
                "goal_amount DECIMAL(10,2) NOT NULL, " +
                "current_amount DECIMAL(10,2) DEFAULT 0, " +
                "creator_id INT NOT NULL, " +
                "status ENUM('active', 'completed') DEFAULT 'active', " +
                "video_url VARCHAR(500), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS donations (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "challenge_id INT NOT NULL, " +
                "donor_name VARCHAR(100) NOT NULL, " +
                "amount DECIMAL(10,2) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE)");

            stmt.executeUpdate("INSERT IGNORE INTO users (id, username, email, password) VALUES " +
                "(1, 'demo_user', 'demo@example.com', '1234'), " +
                "(2, 'challenger99', 'challenger@example.com', '1234')");

            stmt.executeUpdate("INSERT IGNORE INTO challenges (id, title, description, goal_amount, current_amount, creator_id, status, video_url) VALUES " +
                "(1, 'I will learn to play guitar in 30 days', 'I will practice guitar every day for 30 days and record my progress. Help me buy a guitar to start this journey!', 500.00, 320.00, 1, 'active', NULL), " +
                "(2, 'Marathon of 42km in 3 months', 'I will train and complete a full marathon (42km) in 3 months. Every euro helps me buy proper running shoes and equipment!', 300.00, 450.00, 2, 'completed', 'https://www.youtube.com/embed/dQw4w9WgXcQ'), " +
                "(3, 'Read 20 books in 6 months', 'I commit to reading 20 books in 6 months and writing a review for each one. Help me build my home library!', 200.00, 150.00, 1, 'active', NULL)");

            stmt.executeUpdate("INSERT IGNORE INTO donations (id, challenge_id, donor_name, amount) VALUES " +
                "(1, 1, 'Alice', 50.00), " +
                "(2, 1, 'Bob', 100.00), " +
                "(3, 1, 'Charlie', 170.00), " +
                "(4, 2, 'Diana', 100.00), " +
                "(5, 2, 'Eve', 200.00), " +
                "(6, 2, 'Frank', 150.00), " +
                "(7, 3, 'Grace', 75.00), " +
                "(8, 3, 'Heidi', 75.00)");
        } catch (Exception e) {
            event.getServletContext().log("DatabaseInitializer: error initializing schema", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}
