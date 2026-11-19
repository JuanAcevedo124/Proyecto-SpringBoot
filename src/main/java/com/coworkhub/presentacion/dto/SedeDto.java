package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.coworkhub.negocio.model.Location;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/** DTO de sedes. */
public final class SedeDto {

    private SedeDto() {
    }

    @Schema(name = "SedeRequest")
    public record Request(
            @Schema(example = "Sede Chapinero") @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
            @Schema(example = "Bogota") @NotBlank(message = "La ciudad es obligatoria") @Size(max = 80) String ciudad,
            @Schema(example = "Carrera 13 # 60-20") @NotBlank(message = "La direccion es obligatoria") @Size(max = 200) String direccion,
            @Schema(type = "string", example = "07:00") @NotNull(message = "La hora de apertura es obligatoria") LocalTime horaApertura,
            @Schema(type = "string", example = "21:00") @NotNull(message = "La hora de cierre es obligatoria") LocalTime horaCierre) {
    }

    @Schema(name = "SedeResponse")
    public record Response(Long id, String nombre, String ciudad, String direccion,
                           LocalTime horaApertura, LocalTime horaCierre) {

        public static Response de(Location l) {
            return new Response(l.getId(), l.getName(), l.getCity(), l.getAddress(),
                    l.getOpeningTime(), l.getClosingTime());
        }
    }
}
