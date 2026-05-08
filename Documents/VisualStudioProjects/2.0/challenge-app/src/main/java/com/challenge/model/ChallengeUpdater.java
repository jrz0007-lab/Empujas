package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ChallengeUpdater {

    public void completar(int challengeId, String videoUrl) {
        String sql = "UPDATE challenges SET status = 'completed', video_url = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, videoUrl);
            ps.setInt(2, challengeId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error al completar reto", e);
        }
    }
}
