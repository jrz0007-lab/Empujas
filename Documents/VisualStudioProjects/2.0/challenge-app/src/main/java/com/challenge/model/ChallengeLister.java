package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ChallengeLister {

    public List<Challenge> listarTodos(Integer userId) {
        List<Challenge> lista = new ArrayList<>();
        String sql = "SELECT c.*, u.username AS creator_name, "
                + "(SELECT COUNT(*) FROM donations WHERE challenge_id = c.id) AS supporter_count, "
                + (userId != null
                    ? "(SELECT COUNT(*) FROM favorites WHERE challenge_id = c.id AND user_id = " + userId + ") AS is_fav "
                    : "0 AS is_fav ")
                + "FROM challenges c JOIN users u ON c.creator_id = u.id "
                + "ORDER BY c.created_at DESC";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al listar retos", e);
        }

        return lista;
    }

    public List<Challenge> listarTodos() {
        return listarTodos(null);
    }

    public List<Challenge> listarPorCreador(int creatorId, Integer userId) {
        List<Challenge> lista = new ArrayList<>();
        String sql = "SELECT c.*, u.username AS creator_name, "
                + "(SELECT COUNT(*) FROM donations WHERE challenge_id = c.id) AS supporter_count, "
                + (userId != null
                    ? "(SELECT COUNT(*) FROM favorites WHERE challenge_id = c.id AND user_id = " + userId + ") AS is_fav "
                    : "0 AS is_fav ")
                + "FROM challenges c JOIN users u ON c.creator_id = u.id "
                + "WHERE c.creator_id = ? ORDER BY c.created_at DESC";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, creatorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al listar retos por creador", e);
        }

        return lista;
    }

    public List<Challenge> listarPorEstado(String status, Integer userId) {
        List<Challenge> lista = new ArrayList<>();
        String sql = "SELECT c.*, u.username AS creator_name, "
                + "(SELECT COUNT(*) FROM donations WHERE challenge_id = c.id) AS supporter_count, "
                + (userId != null
                    ? "(SELECT COUNT(*) FROM favorites WHERE challenge_id = c.id AND user_id = " + userId + ") AS is_fav "
                    : "0 AS is_fav ")
                + "FROM challenges c JOIN users u ON c.creator_id = u.id "
                + "WHERE c.status = ? ORDER BY c.created_at DESC";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al listar retos por estado", e);
        }

        return lista;
    }

    public List<Challenge> listarPorEstado(String status) {
        return listarPorEstado(status, null);
    }

    public List<Challenge> listarFavoritos(int userId) {
        List<Challenge> lista = new ArrayList<>();
        String sql = "SELECT c.*, u.username AS creator_name, "
                + "(SELECT COUNT(*) FROM donations WHERE challenge_id = c.id) AS supporter_count, "
                + "1 AS is_fav "
                + "FROM challenges c JOIN users u ON c.creator_id = u.id "
                + "JOIN favorites f ON f.challenge_id = c.id "
                + "WHERE f.user_id = ? ORDER BY f.created_at DESC";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al listar favoritos", e);
        }

        return lista;
    }

    private Challenge mapear(ResultSet rs) throws Exception {
        Challenge c = new Challenge();
        c.setId(rs.getInt("id"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setGoalAmount(rs.getDouble("goal_amount"));
        c.setCurrentAmount(rs.getDouble("current_amount"));
        c.setCreatorId(rs.getInt("creator_id"));
        c.setCreatorName(rs.getString("creator_name"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(rs.getString("created_at"));
        c.setSupporterCount(rs.getInt("supporter_count"));
        c.setFavorited(rs.getInt("is_fav") > 0);
        try { c.setVideoUrl(rs.getString("video_url")); } catch (Exception ignored) {}
        try { c.setImageUrl(rs.getString("image_url")); } catch (Exception ignored) {}
        return c;
    }
}
