package com.coworkhub.presentacion.dto;

import com.coworkhub.negocio.model.MembershipPlan;
import jakarta.validation.constraints.*;

/** DTO de planes de membresia. */
public final class PlanDto {

    private PlanDto() {
    }

    public record Request(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String nombre,
            @NotNull(message = "Las horas incluidas son obligatorias") @Min(0) Integer horasIncluidasMes,
            @NotNull(message = "El descuento es obligatorio") @Min(0) @Max(100) Integer descuentoExcedente,
            @NotNull(message = "El maximo de reservas activas es obligatorio") @Min(1) Integer maxReservasActivas) {
    }

    public record Response(Long id, String nombre, int horasIncluidasMes, int descuentoExcedente,
                           int maxReservasActivas) {

        public static Response de(MembershipPlan p) {
            return new Response(p.getId(), p.getName(), p.getIncludedHoursPerMonth(), p.getOverageDiscount(),
                    p.getMaxActiveBookings());
        }
    }
}
