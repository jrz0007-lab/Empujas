package com.challenge.controller;

import com.challenge.model.ReportManager;
import com.challenge.model.UserManager;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/report-challenge")
public class ReportChallengeServlet extends HttpServlet {

    private final ReportManager reportManager = new ReportManager();
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
            String reason = datos != null && datos.get("reason") != null ? datos.get("reason").toString().trim() : "";

            if (challengeId == 0 || userId == 0 || reason.isEmpty()) {
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

            reportManager.reportar(challengeId, userId, reason);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("mensaje", "Reporte enviado correctamente");

            response.getWriter().write(gson.toJson(respuesta));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error al reportar reto");
            error.put("detalle", e.getMessage());

            response.getWriter().write(gson.toJson(error));
        }
    }
}
