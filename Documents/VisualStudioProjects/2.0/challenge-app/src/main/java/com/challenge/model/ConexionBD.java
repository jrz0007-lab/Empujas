package com.challenge.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String DB_HOST = getEnv("DB_HOST", "MYSQL_HOST", "mysql");
    private static final String DB_PORT = getEnv("DB_PORT", "MYSQL_PORT", "3306");
    private static final String DB_NAME = getEnv("DB_NAME", "MYSQL_DATABASE", "challenge_db");
    private static final String DB_USER = getEnv("DB_USER", "MYSQL_USER", "root");
    private static final String DB_PASSWORD = getEnv("DB_PASSWORD", "MYSQL_PASSWORD", "root");

    private static String getEnv(String primary, String fallback, String defaultValue) {
        String val = System.getenv(primary);
        if (val != null && !val.isEmpty()) return val;
        val = System.getenv(fallback);
        if (val != null && !val.isEmpty()) return val;
        return defaultValue;
    }

    private static final String URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se pudo cargar el driver de MySQL", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }
}
