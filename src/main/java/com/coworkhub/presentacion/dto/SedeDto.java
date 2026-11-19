package com.coworkhub.presentacion.dto;

import com.coworkhub.negocio.model.Location;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/** DTO de sedes. */
public final class SedeDto {

    private SedeDto() {
    }

    public record Request(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
            @NotBlank(message = "La ciudad es obligatoria") @Size(max = 80) String ciudad,
            @NotBlank(message = "La direccion es obligatoria") @Size(max = 200) String direccion,
            @NotNull(message = "La hora de apertura es obligatoria") LocalTime horaApertura,
            @NotNull(message = "La hora de cierre es obligatoria") LocalTime horaCierre) {
    }

    public record Response(Long id, String nombre, String ciudad, String direccion,
                           LocalTime horaApertura, LocalTime horaCierre) {

        public static Response de(Location l) {
            return new Response(l.getId(), l.getName(), l.getCity(), l.getAddress(),
                    l.getOpeningTime(), l.getClosingTime());
        }
    }
}
