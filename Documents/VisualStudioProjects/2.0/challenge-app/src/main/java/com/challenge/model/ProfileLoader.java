package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class ProfileLoader {

    public Map<String, Object> cargarResumen(int userId) {
        java.util.Map<String, Object> resumen = new java.util.HashMap<>();

        String sql = "SELECT "
                + "COUNT(*) AS total_created, "
                + "SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) AS total_completed, "
                + "COALESCE(SUM(current_amount), 0) AS total_raised "
                + "FROM challenges WHERE creator_id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resumen.put("totalCreated", rs.getInt("total_created"));
                    resumen.put("totalCompleted", rs.getInt("total_completed"));
                    resumen.put("totalRaised", rs.getDouble("total_raised"));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al cargar perfil", e);
        }

        return resumen;
    }
}
