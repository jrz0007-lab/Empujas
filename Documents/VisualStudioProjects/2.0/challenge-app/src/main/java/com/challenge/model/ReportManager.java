package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ReportManager {

    public void reportar(int challengeId, int reporterId, String reason) {
        String sql = "INSERT INTO reports (challenge_id, reporter_id, reason) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, challengeId);
            ps.setInt(2, reporterId);
            ps.setString(3, reason);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error al reportar reto", e);
        }
    }
}
