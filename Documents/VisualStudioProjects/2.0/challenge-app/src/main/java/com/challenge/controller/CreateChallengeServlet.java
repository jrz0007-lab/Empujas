package com.challenge.controller;

import com.challenge.model.Challenge;
import com.challenge.model.ChallengeCreator;
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

@WebServlet("/api/create-challenge")
public class CreateChallengeServlet extends HttpServlet {

    private final ChallengeCreator challengeCreator = new ChallengeCreator();
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

            String title = datos != null && datos.get("title") != null ? datos.get("title").toString().trim() : "";
            String description = datos != null && datos.get("description") != null ? datos.get("description").toString().trim() : "";
            double goalAmount = datos != null && datos.get("goalAmount") != null ? ((Number) datos.get("goalAmount")).doubleValue() : 0;
            int creatorId = datos != null && datos.get("creatorId") != null ? ((Number) datos.get("creatorId")).intValue() : 0;
            String videoUrl = datos != null && datos.get("videoUrl") != null ? datos.get("videoUrl").toString().trim() : "";
            String imageUrl = datos != null && datos.get("imageUrl") != null ? datos.get("imageUrl").toString().trim() : "";

            if (title.isEmpty() || description.isEmpty() || goalAmount <= 0 || creatorId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("mensaje", "Todos los campos son obligatorios");

                response.getWriter().write(gson.toJson(error));
                return;
            }

            if (userManager.estaBaneado(creatorId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("banned", true);
                error.put("mensaje", "Tu cuenta ha sido baneada. No puedes crear retos.");

                response.getWriter().write(gson.toJson(error));
                return;
            }

            Challenge challenge = challengeCreator.crear(title, description, goalAmount, creatorId,
                videoUrl.isEmpty() ? null : videoUrl,
                imageUrl.isEmpty() ? null : imageUrl);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("mensaje", "Reto creado exitosamente");
            respuesta.put("challenge", challenge);

            response.getWriter().write(gson.toJson(respuesta));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error al crear reto");
            error.put("detalle", e.getMessage());

            response.getWriter().write(gson.toJson(error));
        }
    }
}
