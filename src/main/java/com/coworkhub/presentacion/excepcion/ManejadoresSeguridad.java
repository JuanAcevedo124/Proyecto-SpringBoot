package com.coworkhub.presentacion.excepcion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 401 y 403 que produce Spring Security en la cadena de filtros (antes de llegar a los controladores),
 * con el mismo cuerpo de error que el resto de la API.
 */
@Component
public class ManejadoresSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    /** Atributo que el filtro JWT deja en la solicitud cuando el token es invalido o esta vencido. */
    public static final String ATRIBUTO_ERROR_TOKEN = "coworkhub.errorToken";

    private final FabricaErrores errores;

    public ManejadoresSeguridad(FabricaErrores errores) {
        this.errores = errores;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException {
        Object errorToken = request.getAttribute(ATRIBUTO_ERROR_TOKEN);
        String mensaje = errorToken != null ? errorToken.toString() : "Se requiere autenticacion (token Bearer)";
        errores.escribir(response, HttpServletResponse.SC_UNAUTHORIZED, "NO_AUTENTICADO", mensaje);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException {
        errores.escribir(response, HttpServletResponse.SC_FORBIDDEN, "ACCESO_DENEGADO",
                "No tiene permiso para realizar esta operacion");
    }
}
