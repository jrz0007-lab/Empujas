package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActionManager {

    public void registrarAccion(int adminId, String actionType, Integer targetUserId, String targetUserEmail, Integer targetChallengeId, String reason) {
        String sql = "INSERT INTO admin_actions (admin_id, action_type, target_user_id, target_user_email, target_challenge_id, reason) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, adminId);
            ps.setString(2, actionType);
            if (targetUserId != null) {
                ps.setInt(3, targetUserId);
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setString(4, targetUserEmail);
            if (targetChallengeId != null) {
                ps.setInt(5, targetChallengeId);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.setString(6, reason);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error al registrar accion admin", e);
        }
    }

    public List<Map<String, Object>> obtenerAcciones() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT a.id, a.action_type, a.target_user_id, a.target_user_email, a.target_challenge_id, a.reason, a.created_at, u.username AS admin_name " +
                     "FROM admin_actions a JOIN users u ON a.admin_id = u.id ORDER BY a.created_at DESC LIMIT 100";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> accion = new HashMap<>();
                accion.put("id", rs.getInt("id"));
                accion.put("actionType", rs.getString("action_type"));
                accion.put("targetUserId", rs.getObject("target_user_id"));
                accion.put("targetUserEmail", rs.getString("target_user_email"));
                accion.put("targetChallengeId", rs.getObject("target_challenge_id"));
                accion.put("reason", rs.getString("reason"));
                accion.put("createdAt", rs.getString("created_at"));
                accion.put("adminName", rs.getString("admin_name"));
                lista.add(accion);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener acciones admin", e);
        }

        return lista;
    }

    public List<Map<String, Object>> obtenerReportes() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT r.id, r.reason, r.created_at, " +
                     "u.username AS reporter_name, u.email AS reporter_email, " +
                     "c.id AS challenge_id, c.title AS challenge_title, c.creator_id AS challenge_creator_id " +
                     "FROM reports r " +
                     "JOIN users u ON r.reporter_id = u.id " +
                     "JOIN challenges c ON r.challenge_id = c.id " +
                     "ORDER BY r.created_at DESC LIMIT 100";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> reporte = new HashMap<>();
                reporte.put("id", rs.getInt("id"));
                reporte.put("reason", rs.getString("reason"));
                reporte.put("createdAt", rs.getString("created_at"));
                reporte.put("reporterName", rs.getString("reporter_name"));
                reporte.put("reporterEmail", rs.getString("reporter_email"));
                reporte.put("challengeId", rs.getInt("challenge_id"));
                reporte.put("challengeTitle", rs.getString("challenge_title"));
                reporte.put("challengeCreatorId", rs.getInt("challenge_creator_id"));
                lista.add(reporte);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener reportes", e);
        }

        return lista;
    }
}
