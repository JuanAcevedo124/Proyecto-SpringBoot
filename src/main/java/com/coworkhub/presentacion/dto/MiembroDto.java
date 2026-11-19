package com.coworkhub.presentacion.dto;

import com.coworkhub.negocio.model.Member;
import com.coworkhub.negocio.model.MemberStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** DTO de miembros. La contrasena entra, pero nunca sale. */
public final class MiembroDto {

    private MiembroDto() {
    }

    /** Registro de un miembro junto con su cuenta de usuario (rol MEMBER). */
    public record CrearRequest(
            @NotBlank(message = "El documento es obligatorio") @Size(max = 30) String documento,
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombreCompleto,
            @NotBlank(message = "El email es obligatorio") @Email(message = "El email no es valido") @Size(max = 120) String email,
            @NotNull(message = "El plan es obligatorio") Long planId,
            @NotBlank(message = "El usuario es obligatorio")
            @Pattern(regexp = "^[a-zA-Z0-9._-]{3,50}$", message = "El usuario debe tener entre 3 y 50 caracteres alfanumericos")
            String usuario,
            @NotBlank(message = "La contrasena es obligatoria") @Size(min = 6, max = 72, message = "La contrasena debe tener entre 6 y 72 caracteres")
            String contrasena) {
    }

    public record ActualizarRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombreCompleto,
            @NotBlank(message = "El email es obligatorio") @Email(message = "El email no es valido") @Size(max = 120) String email,
            @NotNull(message = "El plan es obligatorio") Long planId) {
    }

    public record Response(Long id, String documento, String nombreCompleto, String email, MemberStatus estado,
                           Long planId, String planNombre, String usuario) {

        public static Response de(Member m) {
            return new Response(m.getId(), m.getDocumentNumber(), m.getFullName(), m.getEmail(), m.getStatus(),
                    m.getPlan().getId(), m.getPlan().getName(),
                    m.getUserAccount() == null ? null : m.getUserAccount().getUsername());
        }
    }

    /** RF-10: consumo mensual de horas del plan. */
    public record ConsumoResponse(Long miembroId, String mes, String plan, BigDecimal horasIncluidas,
                                  BigDecimal horasUsadas, BigDecimal horasRestantes, BigDecimal horasExcedentes) {
    }
}
