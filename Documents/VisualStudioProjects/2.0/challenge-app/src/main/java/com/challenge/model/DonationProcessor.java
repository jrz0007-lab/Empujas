package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DonationProcessor {

    public void donar(int challengeId, String donorName, double amount, Integer userId) {
        Connection con = null;

        try {
            con = ConexionBD.getConnection();
            con.setAutoCommit(false);

            if (amount < 1) {
                throw new RuntimeException("La donación mínima es de 1€");
            }

            String checkCapSql = "SELECT current_amount, goal_amount, creator_id FROM challenges WHERE id = ? FOR UPDATE";
            double currentAmount;
            double goalAmount;
            int creatorId;
            try (PreparedStatement ps = con.prepareStatement(checkCapSql)) {
                ps.setInt(1, challengeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Reto no encontrado");
                    }
                    currentAmount = rs.getDouble("current_amount");
                    goalAmount = rs.getDouble("goal_amount");
                    creatorId = rs.getInt("creator_id");
                }
            }

            if (userId != null && userId == creatorId) {
                throw new RuntimeException("No puedes donar a tu propio reto");
            }

            if (currentAmount + amount > goalAmount) {
                throw new RuntimeException("El reto ya ha alcanzado su meta de " + String.format("%.2f", goalAmount) + "€. No se aceptan más donaciones.");
            }

            String insertDonacion = "INSERT INTO donations (challenge_id, donor_name, amount, user_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertDonacion)) {
                ps.setInt(1, challengeId);
                ps.setString(2, donorName);
                ps.setDouble(3, amount);
                if (userId != null && userId > 0) {
                    ps.setInt(4, userId);
                } else {
                    ps.setNull(4, java.sql.Types.INTEGER);
                }
                ps.executeUpdate();
            }

            String updateReto = "UPDATE challenges SET current_amount = current_amount + ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(updateReto)) {
                ps.setDouble(1, amount);
                ps.setInt(2, challengeId);
                ps.executeUpdate();
            }

            String checkGoal = "SELECT current_amount, goal_amount FROM challenges WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(checkGoal)) {
                ps.setInt(1, challengeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getDouble("current_amount") >= rs.getDouble("goal_amount")) {
                        String completeSql = "UPDATE challenges SET status = 'completed' WHERE id = ? AND status = 'active'";
                        try (PreparedStatement ps2 = con.prepareStatement(completeSql)) {
                            ps2.setInt(1, challengeId);
                            ps2.executeUpdate();
                        }
                    }
                }
            }

            if (userId != null && userId > 0) {
                String addFav = "INSERT IGNORE INTO favorites (user_id, challenge_id) VALUES (?, ?)";
                try (PreparedStatement ps = con.prepareStatement(addFav)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, challengeId);
                    ps.executeUpdate();
                }
            }

            con.commit();

        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (Exception ex) {}
            }
            throw new RuntimeException("Error al procesar donacion", e);

        } finally {
            if (con != null) {
                try { con.close(); } catch (Exception e) {}
            }
        }
    }
}
