package com.coworkhub.presentacion.dto;

import com.coworkhub.negocio.model.Role;
import jakarta.validation.constraints.NotBlank;

/** DTO de autenticacion. */
public final class AuthDto {

    private AuthDto() {
    }

    public record LoginRequest(
            @NotBlank(message = "El usuario es obligatorio") String usuario,
            @NotBlank(message = "La contrasena es obligatoria") String contrasena) {
    }

    public record LoginResponse(String token, String tipo, long expiraEnSegundos, String usuario, Role rol,
                                Long miembroId) {
    }

    /** RF-14: datos del usuario que inicio sesion. */
    public record PerfilResponse(String usuario, Role rol, Long miembroId) {
    }
}
