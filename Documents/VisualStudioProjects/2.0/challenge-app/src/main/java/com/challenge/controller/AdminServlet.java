package com.challenge.controller;

import com.challenge.model.AdminActionManager;
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

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    private final AdminActionManager adminActionManager = new AdminActionManager();
    private final UserManager userManager = new UserManager();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            String pathInfo = request.getPathInfo();

            if ("/reports".equals(pathInfo)) {
                var reportes = adminActionManager.obtenerReportes();
                Map<String, Object> res = new HashMap<>();
                res.put("ok", true);
                res.put("reportes", reportes);
                response.getWriter().write(gson.toJson(res));

            } else if ("/banned-users".equals(pathInfo)) {
                var baneados = userManager.obtenerBaneados();
                Map<String, Object> res = new HashMap<>();
                res.put("ok", true);
                res.put("usuarios", baneados);
                response.getWriter().write(gson.toJson(res));

            } else if ("/actions".equals(pathInfo)) {
                var acciones = adminActionManager.obtenerAcciones();
                Map<String, Object> res = new HashMap<>();
                res.put("ok", true);
                res.put("acciones", acciones);
                response.getWriter().write(gson.toJson(res));

            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("mensaje", "Endpoint no encontrado");
                response.getWriter().write(gson.toJson(error));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error en admin endpoint");
            error.put("detalle", e.getMessage());
            response.getWriter().write(gson.toJson(error));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            String pathInfo = request.getPathInfo();

            if ("/unban".equals(pathInfo)) {
                StringBuilder jsonRecibido = new StringBuilder();
                BufferedReader reader = request.getReader();
                String linea;

                while ((linea = reader.readLine()) != null) {
                    jsonRecibido.append(linea);
                }

                Map<String, Object> datos = gson.fromJson(jsonRecibido.toString(), Map.class);

                int adminUserId = datos != null && datos.get("adminUserId") != null ? ((Number) datos.get("adminUserId")).intValue() : 0;
                int targetUserId = datos != null && datos.get("targetUserId") != null ? ((Number) datos.get("targetUserId")).intValue() : 0;

                if (adminUserId == 0 || targetUserId == 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, Object> error = new HashMap<>();
                    error.put("ok", false);
                    error.put("mensaje", "Datos incompletos");
                    response.getWriter().write(gson.toJson(error));
                    return;
                }

                userManager.desbanear(adminUserId, targetUserId);

                Map<String, Object> res = new HashMap<>();
                res.put("ok", true);
                res.put("mensaje", "Usuario desbaneado correctamente");
                response.getWriter().write(gson.toJson(res));

            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                Map<String, Object> error = new HashMap<>();
                error.put("ok", false);
                error.put("mensaje", "Endpoint no encontrado");
                response.getWriter().write(gson.toJson(error));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error en admin endpoint");
            error.put("detalle", e.getMessage());
            response.getWriter().write(gson.toJson(error));
        }
    }
}
