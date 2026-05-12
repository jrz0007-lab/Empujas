package com.challenge.controller;

import com.challenge.model.AdminActionManager;
import com.challenge.model.ChallengeDeleter;
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
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.challenge.model.ConexionBD;

@WebServlet("/api/delete-challenge")
public class DeleteChallengeServlet extends HttpServlet {

    private final ChallengeDeleter challengeDeleter = new ChallengeDeleter();
    private final UserManager userManager = new UserManager();
    private final AdminActionManager adminActionManager = new AdminActionManager();
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
            String reason = datos != null && datos.get("reason") != null ? (String) datos.get("reason") : "";

            if (challengeId == 0 || userId == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("mensaje", "Datos incompletos");

                response.getWriter().write(gson.toJson(error));
                return;
            }

            if (!userManager.esAdmin(userId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("mensaje", "No tienes permisos de administrador");

                response.getWriter().write(gson.toJson(error));
                return;
            }

            if (reason.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("mensaje", "Debes proporcionar un motivo para eliminar el reto");

                response.getWriter().write(gson.toJson(error));
                return;
            }

            int creatorId = 0;
            String creatorEmail = null;
            String getChallengeSql = "SELECT creator_id FROM challenges WHERE id = ?";
            try (Connection con = ConexionBD.getConnection();
                 PreparedStatement ps = con.prepareStatement(getChallengeSql)) {
                ps.setInt(1, challengeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        creatorId = rs.getInt("creator_id");
                    }
                }
            }

            if (creatorId > 0) {
                creatorEmail = userManager.obtenerEmailPorId(creatorId);
            }

            challengeDeleter.eliminarComoAdmin(challengeId);
            adminActionManager.registrarAccion(userId, "delete_challenge", creatorId, creatorEmail, challengeId, reason);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("mensaje", "Reto eliminado correctamente");

            response.getWriter().write(gson.toJson(respuesta));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error al eliminar reto");
            error.put("detalle", e.getMessage());

            response.getWriter().write(gson.toJson(error));
        }
    }
}
