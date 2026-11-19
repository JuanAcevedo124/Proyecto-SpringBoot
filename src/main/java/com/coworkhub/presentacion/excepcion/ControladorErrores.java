package com.coworkhub.presentacion.excepcion;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reemplaza el /error por defecto de Spring Boot para que los errores que no pasan por el
 * @RestControllerAdvice (los que ocurren en el contenedor de servlets) tambien usen el cuerpo estandar.
 */
@RestController
public class ControladorErrores implements ErrorController {

    private final FabricaErrores errores;

    public ControladorErrores(FabricaErrores errores) {
        this.errores = errores;
    }

    @RequestMapping("/error")
    public ResponseEntity<ErrorResponse> error(HttpServletRequest request) {
        Object codigo = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = codigo instanceof Integer c ? HttpStatus.resolve(c) : null;
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String tipo = switch (status) {
            case NOT_FOUND -> "RUTA_NO_ENCONTRADA";
            case METHOD_NOT_ALLOWED -> "METODO_NO_PERMITIDO";
            case BAD_REQUEST -> "SOLICITUD_INVALIDA";
            case UNAUTHORIZED -> "NO_AUTENTICADO";
            case FORBIDDEN -> "ACCESO_DENEGADO";
            default -> status.is5xxServerError() ? "ERROR_INTERNO" : "ERROR";
        };
        String mensaje = status.is5xxServerError() ? "Ocurrio un error inesperado" : status.getReasonPhrase();
        return ResponseEntity.status(status).body(errores.crear(tipo, mensaje));
    }
}
