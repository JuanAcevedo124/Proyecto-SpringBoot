package com.coworkhub.presentacion.dto;

import com.coworkhub.negocio.model.AdditionalService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** DTO de servicios adicionales. */
public final class ServicioAdicionalDto {

    private ServicioAdicionalDto() {
    }

    public record Request(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String nombre,
            @NotNull(message = "El precio unitario es obligatorio")
            @DecimalMin(value = "0.0", message = "El precio no puede ser negativo") BigDecimal precioUnitario) {
    }

    public record Response(Long id, String nombre, BigDecimal precioUnitario) {

        public static Response de(AdditionalService s) {
            return new Response(s.getId(), s.getName(), s.getUnitPrice());
        }
    }
}
