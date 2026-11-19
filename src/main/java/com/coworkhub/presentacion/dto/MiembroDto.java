package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(name = "MiembroCrearRequest")
    public record CrearRequest(
            @Schema(example = "1020304050") @NotBlank(message = "El documento es obligatorio") @Size(max = 30) String documento,
            @Schema(example = "Marta Lopez") @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombreCompleto,
            @Schema(example = "marta.lopez@example.com") @NotBlank(message = "El email es obligatorio") @Email(message = "El email no es valido") @Size(max = 120) String email,
            @Schema(example = "2") @NotNull(message = "El plan es obligatorio") Long planId,
            @Schema(example = "marta") @NotBlank(message = "El usuario es obligatorio")
            @Pattern(regexp = "^[a-zA-Z0-9._-]{3,50}$", message = "El usuario debe tener entre 3 y 50 caracteres alfanumericos")
            String usuario,
            @Schema(example = "marta123") @NotBlank(message = "La contrasena es obligatoria") @Size(min = 6, max = 72, message = "La contrasena debe tener entre 6 y 72 caracteres")
            String contrasena) {
    }

    @Schema(name = "MiembroActualizarRequest")
    public record ActualizarRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombreCompleto,
            @NotBlank(message = "El email es obligatorio") @Email(message = "El email no es valido") @Size(max = 120) String email,
            @NotNull(message = "El plan es obligatorio") Long planId) {
    }

    @Schema(name = "MiembroResponse")
    public record Response(Long id, String documento, String nombreCompleto, String email, MemberStatus estado,
                           Long planId, String planNombre, String usuario) {

        public static Response de(Member m) {
            return new Response(m.getId(), m.getDocumentNumber(), m.getFullName(), m.getEmail(), m.getStatus(),
                    m.getPlan().getId(), m.getPlan().getName(),
                    m.getUserAccount() == null ? null : m.getUserAccount().getUsername());
        }
    }

    /** RF-10: consumo mensual de horas del plan. */
    @Schema(name = "ConsumoResponse")
    public record ConsumoResponse(Long miembroId, String mes, String plan, BigDecimal horasIncluidas,
                                  BigDecimal horasUsadas, BigDecimal horasRestantes, BigDecimal horasExcedentes) {
    }
}
