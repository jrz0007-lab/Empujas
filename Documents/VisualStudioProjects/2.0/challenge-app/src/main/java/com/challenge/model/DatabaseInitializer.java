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
                "is_admin TINYINT(1) DEFAULT 0, " +
                "banned TINYINT(1) DEFAULT 0, " +
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
                "image_url VARCHAR(500), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS donations (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "challenge_id INT NOT NULL, " +
                "donor_name VARCHAR(100) NOT NULL, " +
                "amount DECIMAL(10,2) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE)");

            try { stmt.executeUpdate("ALTER TABLE challenges ADD COLUMN image_url VARCHAR(500)"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE challenges ADD COLUMN video_url VARCHAR(500)"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE users ADD COLUMN is_admin TINYINT(1) DEFAULT 0"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE users ADD COLUMN banned TINYINT(1) DEFAULT 0"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE users ADD COLUMN ban_reason TEXT DEFAULT NULL"); } catch (Exception ignored) {}

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS favorites (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "challenge_id INT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE, " +
                "UNIQUE KEY (user_id, challenge_id))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS reports (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "challenge_id INT NOT NULL, " +
                "reporter_id INT NOT NULL, " +
                "reason TEXT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS admin_actions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "admin_id INT NOT NULL, " +
                "action_type VARCHAR(30) NOT NULL, " +
                "target_user_id INT, " +
                "target_user_email VARCHAR(255), " +
                "target_challenge_id INT, " +
                "reason TEXT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE)");

            stmt.executeUpdate("UPDATE users SET password = '1234' WHERE email IN ('demo@example.com', 'challenger@example.com')");
            stmt.executeUpdate("INSERT INTO users (id, username, email, password, is_admin) VALUES " +
                "(1, 'demo_user', 'demo@example.com', '1234', 0), " +
                "(2, 'challenger99', 'challenger@example.com', '1234', 0), " +
                "(99, 'admin', 'admin@pujas.com', 'admin123', 1) " +
                "ON DUPLICATE KEY UPDATE password = VALUES(password), is_admin = VALUES(is_admin)");

            stmt.executeUpdate("UPDATE users SET is_admin = 1 WHERE email LIKE '%@pujas.com'");

            try {
                stmt.executeUpdate("INSERT IGNORE INTO challenges (id, title, description, goal_amount, current_amount, creator_id, status, video_url, image_url) VALUES " +
                    "(1, 'I will learn to play guitar in 30 days', 'I will practice guitar every day for 30 days and record my progress. Help me buy a guitar to start this journey!', 500.00, 320.00, 1, 'active', NULL, NULL), " +
                    "(2, 'Marathon of 42km in 3 months', 'I will train and complete a full marathon (42km) in 3 months. Every euro helps me buy proper running shoes and equipment!', 300.00, 450.00, 2, 'completed', 'https://www.youtube.com/embed/dQw4w9WgXcQ', NULL), " +
                    "(3, 'Read 20 books in 6 months', 'I commit to reading 20 books in 6 months and writing a review for each one. Help me build my home library!', 200.00, 150.00, 1, 'active', NULL, NULL)");
            } catch (Exception ignored) {
                stmt.executeUpdate("INSERT IGNORE INTO challenges (id, title, description, goal_amount, current_amount, creator_id, status, video_url) VALUES " +
                    "(1, 'I will learn to play guitar in 30 days', 'I will practice guitar every day for 30 days and record my progress. Help me buy a guitar to start this journey!', 500.00, 320.00, 1, 'active', NULL), " +
                    "(2, 'Marathon of 42km in 3 months', 'I will train and complete a full marathon (42km) in 3 months. Every euro helps me buy proper running shoes and equipment!', 300.00, 450.00, 2, 'completed', 'https://www.youtube.com/embed/dQw4w9WgXcQ'), " +
                    "(3, 'Read 20 books in 6 months', 'I commit to reading 20 books in 6 months and writing a review for each one. Help me build my home library!', 200.00, 150.00, 1, 'active', NULL)");
            }

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
