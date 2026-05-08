package com.challenge.controller;

import com.challenge.model.Challenge;
import com.challenge.model.ChallengeLister;
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

@WebServlet("/api/challenges")
public class ListChallengesServlet extends HttpServlet {

    private final ChallengeLister challengeLister = new ChallengeLister();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            String status = request.getParameter("status");
            String creatorId = request.getParameter("creatorId");

            List<Challenge> resultados;

            if (creatorId != null && !creatorId.isEmpty()) {
                resultados = challengeLister.listarPorCreador(Integer.parseInt(creatorId));
            } else if (status != null && !status.isEmpty()) {
                resultados = challengeLister.listarPorEstado(status);
            } else {
                resultados = challengeLister.listarTodos();
            }

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("total", resultados.size());
            respuesta.put("resultados", resultados);

            response.getWriter().write(gson.toJson(respuesta));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error al obtener retos");
            error.put("detalle", e.getMessage());

            response.getWriter().write(gson.toJson(error));
        }
    }
}
