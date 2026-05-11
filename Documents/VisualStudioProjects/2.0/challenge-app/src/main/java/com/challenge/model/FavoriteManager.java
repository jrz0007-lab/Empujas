package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class FavoriteManager {

    public void toggle(int userId, int challengeId) {
        String checkSql = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND challenge_id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(checkSql)) {

            ps.setInt(1, userId);
            ps.setInt(2, challengeId);
            var rs = ps.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                String deleteSql = "DELETE FROM favorites WHERE user_id = ? AND challenge_id = ?";
                try (PreparedStatement ps2 = con.prepareStatement(deleteSql)) {
                    ps2.setInt(1, userId);
                    ps2.setInt(2, challengeId);
                    ps2.executeUpdate();
                }
            } else {
                String insertSql = "INSERT INTO favorites (user_id, challenge_id) VALUES (?, ?)";
                try (PreparedStatement ps2 = con.prepareStatement(insertSql)) {
                    ps2.setInt(1, userId);
                    ps2.setInt(2, challengeId);
                    ps2.executeUpdate();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al alternar favorito", e);
        }
    }

    public void addFavorite(int userId, int challengeId) {
        String sql = "INSERT IGNORE INTO favorites (user_id, challenge_id) VALUES (?, ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, challengeId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error al añadir favorito", e);
        }
    }
}
