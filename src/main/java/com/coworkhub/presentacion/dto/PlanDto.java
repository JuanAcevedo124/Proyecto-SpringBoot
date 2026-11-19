package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.coworkhub.negocio.model.MembershipPlan;
import jakarta.validation.constraints.*;

/** DTO de planes de membresia. */
public final class PlanDto {

    private PlanDto() {
    }

    @Schema(name = "PlanRequest")
    public record Request(
            @Schema(example = "Empresarial") @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String nombre,
            @Schema(example = "30") @NotNull(message = "Las horas incluidas son obligatorias") @Min(0) Integer horasIncluidasMes,
            @Schema(description = "Porcentaje 0-100 sobre las horas excedentes", example = "15") @NotNull(message = "El descuento es obligatorio") @Min(0) @Max(100) Integer descuentoExcedente,
            @Schema(example = "8") @NotNull(message = "El maximo de reservas activas es obligatorio") @Min(1) Integer maxReservasActivas) {
    }

    @Schema(name = "PlanResponse")
    public record Response(Long id, String nombre, int horasIncluidasMes, int descuentoExcedente,
                           int maxReservasActivas) {

        public static Response de(MembershipPlan p) {
            return new Response(p.getId(), p.getName(), p.getIncludedHoursPerMonth(), p.getOverageDiscount(),
                    p.getMaxActiveBookings());
        }
    }
}
