package com.challenge.controller;

import com.challenge.model.FavoriteManager;
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

@WebServlet("/api/toggle-favorite")
public class ToggleFavoriteServlet extends HttpServlet {

    private final FavoriteManager favoriteManager = new FavoriteManager();
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

            int userId = datos != null && datos.get("userId") != null ? ((Number) datos.get("userId")).intValue() : 0;
            int challengeId = datos != null && datos.get("challengeId") != null ? ((Number) datos.get("challengeId")).intValue() : 0;

            if (userId <= 0 || challengeId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("mensaje", "Usuario y reto requeridos");

                response.getWriter().write(gson.toJson(error));
                return;
            }

            favoriteManager.toggle(userId, challengeId);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("mensaje", "Favorito actualizado");

            response.getWriter().write(gson.toJson(respuesta));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error al alternar favorito");
            error.put("detalle", e.getMessage());

            response.getWriter().write(gson.toJson(error));
        }
    }
}
