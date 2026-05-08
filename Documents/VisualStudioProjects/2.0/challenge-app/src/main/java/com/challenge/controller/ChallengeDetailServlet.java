package com.challenge.controller;

import com.challenge.model.Challenge;
import com.challenge.model.ChallengeFinder;
import com.challenge.model.Donation;
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

@WebServlet("/api/challenge")
public class ChallengeDetailServlet extends HttpServlet {

    private final ChallengeFinder challengeFinder = new ChallengeFinder();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            String idStr = request.getParameter("id");

            if (idStr == null || idStr.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("mensaje", "ID de reto requerido");

                response.getWriter().write(gson.toJson(error));
                return;
            }

            int id = Integer.parseInt(idStr);
            Challenge challenge = challengeFinder.buscarPorId(id);

            if (challenge == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);

                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("mensaje", "Reto no encontrado");

                response.getWriter().write(gson.toJson(error));
                return;
            }

            List<Donation> donaciones = challengeFinder.buscarDonaciones(id);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("challenge", challenge);
            respuesta.put("donations", donaciones);

            response.getWriter().write(gson.toJson(respuesta));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error al obtener detalle del reto");
            error.put("detalle", e.getMessage());

            response.getWriter().write(gson.toJson(error));
        }
    }
}
