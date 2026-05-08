package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ChallengeFinder {

    public Challenge buscarPorId(int id) {
        String sql = "SELECT c.*, u.username AS creator_name, "
                + "(SELECT COUNT(*) FROM donations WHERE challenge_id = c.id) AS supporter_count "
                + "FROM challenges c JOIN users u ON c.creator_id = u.id "
                + "WHERE c.id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Challenge c = new Challenge();
                    c.setId(rs.getInt("id"));
                    c.setTitle(rs.getString("title"));
                    c.setDescription(rs.getString("description"));
                    c.setGoalAmount(rs.getDouble("goal_amount"));
                    c.setCurrentAmount(rs.getDouble("current_amount"));
                    c.setCreatorId(rs.getInt("creator_id"));
                    c.setCreatorName(rs.getString("creator_name"));
                    c.setStatus(rs.getString("status"));
                    c.setVideoUrl(rs.getString("video_url"));
                    c.setCreatedAt(rs.getString("created_at"));
                    c.setSupporterCount(rs.getInt("supporter_count"));
                    return c;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al buscar reto por id", e);
        }

        return null;
    }

    public List<Donation> buscarDonaciones(int challengeId) {
        List<Donation> lista = new ArrayList<>();
        String sql = "SELECT * FROM donations WHERE challenge_id = ? ORDER BY created_at DESC";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, challengeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Donation d = new Donation();
                    d.setId(rs.getInt("id"));
                    d.setChallengeId(rs.getInt("challenge_id"));
                    d.setDonorName(rs.getString("donor_name"));
                    d.setAmount(rs.getDouble("amount"));
                    d.setCreatedAt(rs.getString("created_at"));
                    lista.add(d);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al buscar donaciones", e);
        }

        return lista;
    }
}
