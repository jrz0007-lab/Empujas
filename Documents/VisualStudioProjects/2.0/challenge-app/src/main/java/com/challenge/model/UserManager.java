package com.challenge.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserManager {

    public void banear(int adminUserId, int targetUserId, String reason) {
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

        eliminarRetosDeUsuario(targetUserId);

        String softBan = "UPDATE users SET banned = 1, ban_reason = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(softBan)) {

            ps.setString(1, reason);
            ps.setInt(2, targetUserId);
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

    public void eliminarRetosDeUsuario(int userId) {
        String sql = "DELETE FROM challenges WHERE creator_id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar retos del usuario", e);
        }
    }

    public boolean estaBaneado(int userId) {
        String sql = "SELECT banned FROM users WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("banned") == 1;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al verificar ban", e);
        }
    }

    public List<Map<String, Object>> obtenerBaneados() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT id, username, email, ban_reason FROM users WHERE banned = 1";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> user = new java.util.HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                user.put("banReason", rs.getString("ban_reason"));
                lista.add(user);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener baneados", e);
        }

        return lista;
    }

    public void desbanear(int adminUserId, int targetUserId) {
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

        String sql = "UPDATE users SET banned = 0, ban_reason = NULL WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, targetUserId);
            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new RuntimeException("Usuario no encontrado");
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al desbanear usuario", e);
        }
    }

    public String obtenerEmailPorId(int userId) {
        String sql = "SELECT email FROM users WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener email", e);
        }

        return null;
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
