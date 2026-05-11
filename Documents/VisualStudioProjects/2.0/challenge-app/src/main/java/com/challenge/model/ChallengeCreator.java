package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

public class ChallengeCreator {

    public Challenge crear(String title, String description, double goalAmount, int creatorId, String videoUrl, String imageUrl) {
        String sql = "INSERT INTO challenges (title, description, goal_amount, creator_id, video_url, image_url) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, title);
            ps.setString(2, description);
            ps.setDouble(3, goalAmount);
            ps.setInt(4, creatorId);
            ps.setString(5, videoUrl);
            ps.setString(6, imageUrl);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    ChallengeFinder finder = new ChallengeFinder();
                    return finder.buscarPorId(rs.getInt(1));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al crear reto", e);
        }

        return null;
    }
}
