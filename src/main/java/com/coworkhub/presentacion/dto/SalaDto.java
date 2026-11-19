package com.coworkhub.presentacion.dto;

import com.coworkhub.negocio.model.Room;
import com.coworkhub.negocio.model.RoomType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** DTO de salas. */
public final class SalaDto {

    private SalaDto() {
    }

    public record Request(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
            @NotNull(message = "El tipo es obligatorio") RoomType tipo,
            @NotNull(message = "La capacidad es obligatoria") @Min(value = 1, message = "La capacidad minima es 1") Integer capacidad,
            @NotNull(message = "La tarifa por hora es obligatoria")
            @DecimalMin(value = "0.0", message = "La tarifa no puede ser negativa") BigDecimal tarifaHora,
            Boolean activa,
            @NotNull(message = "La sede es obligatoria") Long sedeId,
            Set<Long> equipamientoIds) {
    }

    public record Response(Long id, String nombre, RoomType tipo, int capacidad, BigDecimal tarifaHora,
                           boolean activa, Long sedeId, String sedeNombre,
                           List<EquipamientoDto.Response> equipamiento) {

        public static Response de(Room r) {
            List<EquipamientoDto.Response> equipos = r.getEquipment().stream()
                    .map(EquipamientoDto.Response::de)
                    .sorted(Comparator.comparing(EquipamientoDto.Response::id))
                    .toList();
            return new Response(r.getId(), r.getName(), r.getType(), r.getCapacity(), r.getHourlyRate(),
                    r.isActive(), r.getLocation().getId(), r.getLocation().getName(), equipos);
        }
    }
}
