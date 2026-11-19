package com.coworkhub.presentacion.excepcion;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cuerpo unico de todas las respuestas de error.
 * Ejemplo: {"error": "La sala ya esta reservada en ese horario", "codigo": "RN-01", "timestamp": "2030-06-03T13:00:00Z"}
 */
@Schema(name = "ErrorResponse", description = "Cuerpo estandar de todos los errores")
public record ErrorResponse(
        @Schema(description = "Mensaje legible", example = "La sala ya esta reservada en ese horario") String error,
        @Schema(description = "Regla violada (RN-xx) o tipo de error", example = "RN-01") String codigo,
        @Schema(description = "Momento del error, en UTC", example = "2030-06-03T13:00:00Z") String timestamp) {
}
