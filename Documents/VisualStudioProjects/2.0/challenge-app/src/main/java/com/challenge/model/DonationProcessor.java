package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DonationProcessor {

    public void donar(int challengeId, String donorName, double amount) {
        Connection con = null;

        try {
            con = ConexionBD.getConnection();
            con.setAutoCommit(false);

            String insertDonacion = "INSERT INTO donations (challenge_id, donor_name, amount) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertDonacion)) {
                ps.setInt(1, challengeId);
                ps.setString(2, donorName);
                ps.setDouble(3, amount);
                ps.executeUpdate();
            }

            String updateReto = "UPDATE challenges SET current_amount = current_amount + ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(updateReto)) {
                ps.setDouble(1, amount);
                ps.setInt(2, challengeId);
                ps.executeUpdate();
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
