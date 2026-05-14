package com.challenge.controller;

import com.challenge.model.DonationProcessor;
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

@WebServlet("/api/donate")
public class DonateServlet extends HttpServlet {

    private final DonationProcessor donationProcessor = new DonationProcessor();
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
            String donorName = datos != null && datos.get("donorName") != null ? datos.get("donorName").toString() : "";
            double amount = datos != null && datos.get("amount") != null ? ((Number) datos.get("amount")).doubleValue() : 0;
            Integer userId = datos != null && datos.get("userId") != null ? ((Number) datos.get("userId")).intValue() : null;

            if (challengeId <= 0 || donorName.isEmpty() || amount <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("mensaje", "Todos los campos son obligatorios");

                response.getWriter().write(gson.toJson(error));
                return;
            }

            if (amount < 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("mensaje", "La donación mínima es de 1€");
                response.getWriter().write(gson.toJson(error));
                return;
            }

            if (userId != null && userManager.estaBaneado(userId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("banned", true);
                error.put("mensaje", "Tu cuenta ha sido baneada. No puedes donar.");
                response.getWriter().write(gson.toJson(error));
                return;
            }

            donationProcessor.donar(challengeId, donorName, amount, userId);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("mensaje", "Donación procesada exitosamente");

            response.getWriter().write(gson.toJson(respuesta));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error al procesar donación");
            error.put("detalle", e.getMessage());

            response.getWriter().write(gson.toJson(error));
        }
    }
}
