package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserAuthenticator {

    public User autenticar(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt("banned") == 1) {
                        return null;
                    }
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setAdmin(rs.getInt("is_admin") == 1);
                    user.setBanned(rs.getInt("banned") == 1);
                    return user;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al autenticar usuario", e);
        }

        return null;
    }
}
