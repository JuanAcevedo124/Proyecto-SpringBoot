package com.coworkhub.presentacion.excepcion;

/**
 * Cuerpo unico de todas las respuestas de error.
 * Ejemplo: {"error": "La sala ya esta reservada en ese horario", "codigo": "RN-01", "timestamp": "2030-06-03T13:00:00Z"}
 */
public record ErrorResponse(String error, String codigo, String timestamp) {
}
