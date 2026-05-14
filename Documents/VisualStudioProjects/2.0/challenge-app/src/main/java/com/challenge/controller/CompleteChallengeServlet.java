package com.challenge.controller;

import com.challenge.model.UserManager;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import com.challenge.model.ConexionBD;

@WebServlet("/api/complete-challenge")
public class CompleteChallengeServlet extends HttpServlet {

    private final UserManager userManager = new UserManager();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            StringBuilder jsonRecibido = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;
            while ((linea = reader.readLine()) != null) {
                jsonRecibido.append(linea);
            }

            Map<String, Object> datos = gson.fromJson(jsonRecibido.toString(), Map.class);

            int challengeId = datos != null && datos.get("challengeId") != null ? ((Number) datos.get("challengeId")).intValue() : 0;
            int userId = datos != null && datos.get("userId") != null ? ((Number) datos.get("userId")).intValue() : 0;
            String completionVideoUrl = datos != null && datos.get("completionVideoUrl") != null ? datos.get("completionVideoUrl").toString().trim() : "";
            String thankYouMessage = datos != null && datos.get("thankYouMessage") != null ? datos.get("thankYouMessage").toString().trim() : "";

            if (challengeId <= 0 || userId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("mensaje", "Datos incompletos");
                response.getWriter().write(gson.toJson(error));
                return;
            }

            if (userManager.estaBaneado(userId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("banned", true);
                error.put("mensaje", "Tu cuenta ha sido baneada.");
                response.getWriter().write(gson.toJson(error));
                return;
            }

            String checkCreator = "SELECT creator_id, status FROM challenges WHERE id = ?";
            try (Connection con = ConexionBD.getConnection();
                 PreparedStatement ps = con.prepareStatement(checkCreator)) {
                ps.setInt(1, challengeId);
                try (var rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        Map<String, Object> error = new HashMap<>();
                        error.put("ok", false);
                        error.put("mensaje", "Reto no encontrado");
                        response.getWriter().write(gson.toJson(error));
                        return;
                    }
                    if (rs.getInt("creator_id") != userId) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        Map<String, Object> error = new HashMap<>();
                        error.put("ok", false);
                        error.put("mensaje", "Solo el creador del reto puede añadir el video de logro");
                        response.getWriter().write(gson.toJson(error));
                        return;
                    }
                    if (!"completed".equals(rs.getString("status"))) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        Map<String, Object> error = new HashMap<>();
                        error.put("ok", false);
                        error.put("mensaje", "El reto debe estar completado para añadir un video de logro");
                        response.getWriter().write(gson.toJson(error));
                        return;
                    }
                }
            }

            String sql = "UPDATE challenges SET completion_video_url = ?, thank_you_message = ? WHERE id = ?";
            try (Connection con = ConexionBD.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, completionVideoUrl.isEmpty() ? null : completionVideoUrl);
                ps.setString(2, thankYouMessage.isEmpty() ? null : thankYouMessage);
                ps.setInt(3, challengeId);
                ps.executeUpdate();
            }

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("mensaje", "Video de logro y agradecimiento actualizados correctamente");
            response.getWriter().write(gson.toJson(respuesta));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error al actualizar el reto");
            error.put("detalle", e.getMessage());
            response.getWriter().write(gson.toJson(error));
        }
    }
}
