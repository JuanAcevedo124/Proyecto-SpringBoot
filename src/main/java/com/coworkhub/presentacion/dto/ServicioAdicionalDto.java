package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(name = "ServicioAdicionalRequest")
    public record Request(
            @Schema(example = "Coffee break") @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String nombre,
            @Schema(example = "15000.00") @NotNull(message = "El precio unitario es obligatorio")
            @DecimalMin(value = "0.0", message = "El precio no puede ser negativo") BigDecimal precioUnitario) {
    }

    @Schema(name = "ServicioAdicionalResponse")
    public record Response(Long id, String nombre, BigDecimal precioUnitario) {

        public static Response de(AdditionalService s) {
            return new Response(s.getId(), s.getName(), s.getUnitPrice());
        }
    }
}
