package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class UserRegistrar {

    public User registrar(String username, String email, String password) {
        int isAdmin = email.toLowerCase().endsWith("@pujas.com") ? 1 : 0;
        String sql = "INSERT INTO users (username, email, password, is_admin) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setInt(4, isAdmin);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt(1));
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setPassword(password);
                    user.setAdmin(isAdmin == 1);
                    return user;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al registrar usuario", e);
        }

        return null;
    }
}
