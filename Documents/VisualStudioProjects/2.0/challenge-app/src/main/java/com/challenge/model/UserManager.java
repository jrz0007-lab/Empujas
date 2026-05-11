package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserManager {

    public void banear(int adminUserId, int targetUserId) {
        String checkAdmin = "SELECT is_admin FROM users WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(checkAdmin)) {

            ps.setInt(1, adminUserId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt("is_admin") != 1) {
                    throw new RuntimeException("No tienes permisos de administrador");
                }
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al verificar permisos", e);
        }

        String deleteUser = "DELETE FROM users WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(deleteUser)) {

            ps.setInt(1, targetUserId);
            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new RuntimeException("Usuario no encontrado");
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al banear usuario", e);
        }
    }

    public boolean esAdmin(int userId) {
        String sql = "SELECT is_admin FROM users WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("is_admin") == 1;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al verificar admin", e);
        }
    }
}
