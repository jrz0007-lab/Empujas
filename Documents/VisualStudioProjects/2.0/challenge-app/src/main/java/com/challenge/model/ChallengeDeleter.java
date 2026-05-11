package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ChallengeDeleter {

    public void eliminar(int challengeId, int creatorId) {
        String sql = "DELETE FROM challenges WHERE id = ? AND creator_id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, challengeId);
            ps.setInt(2, creatorId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar reto", e);
        }
    }

    public void eliminarComoAdmin(int challengeId) {
        String sql = "DELETE FROM challenges WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, challengeId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar reto como admin", e);
        }
    }
}
