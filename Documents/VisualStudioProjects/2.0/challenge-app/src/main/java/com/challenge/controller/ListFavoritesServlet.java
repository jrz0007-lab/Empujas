package com.challenge.controller;

import com.challenge.model.Challenge;
import com.challenge.model.ChallengeLister;
import com.challenge.model.UserManager;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/favorites")
public class ListFavoritesServlet extends HttpServlet {

    private final ChallengeLister challengeLister = new ChallengeLister();
    private final UserManager userManager = new UserManager();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            String userIdParam = request.getParameter("userId");

            if (userIdParam == null || userIdParam.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("mensaje", "Usuario requerido");

                response.getWriter().write(gson.toJson(error));
                return;
            }

            int userId = Integer.parseInt(userIdParam);

            if (userManager.estaBaneado(userId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("banned", true);
                error.put("mensaje", "Tu cuenta ha sido baneada.");

                response.getWriter().write(gson.toJson(error));
                return;
            }

            List<Challenge> resultados = challengeLister.listarFavoritos(userId);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("total", resultados.size());
            respuesta.put("resultados", resultados);

            response.getWriter().write(gson.toJson(respuesta));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error al obtener favoritos");
            error.put("detalle", e.getMessage());

            response.getWriter().write(gson.toJson(error));
        }
    }
}
