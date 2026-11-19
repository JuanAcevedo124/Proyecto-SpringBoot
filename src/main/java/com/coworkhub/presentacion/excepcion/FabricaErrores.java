package com.coworkhub.presentacion.excepcion;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Construye el cuerpo de error estandar. Lo usan el @RestControllerAdvice, el controlador de /error
 * y los manejadores 401/403 de Spring Security (que actuan antes de llegar a Spring MVC).
 */
@Component
public class FabricaErrores {

    private final Clock relojUtc;

    public FabricaErrores(@Qualifier("relojUtc") Clock relojUtc) {
        this.relojUtc = relojUtc;
    }

    public ErrorResponse crear(String codigo, String mensaje) {
        return new ErrorResponse(mensaje, codigo, Instant.now(relojUtc).truncatedTo(ChronoUnit.SECONDS).toString());
    }

    /** Escribe el error directamente en la respuesta (para los filtros de seguridad). */
    public void escribir(HttpServletResponse response, int status, String codigo, String mensaje) throws IOException {
        ErrorResponse e = crear(codigo, mensaje);
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"error\":\"" + json(e.error()) + "\",\"codigo\":\"" + json(e.codigo())
                + "\",\"timestamp\":\"" + e.timestamp() + "\"}");
    }

    private static String json(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
