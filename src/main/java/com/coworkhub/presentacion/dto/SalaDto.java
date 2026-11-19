package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(name = "SalaRequest")
    public record Request(
            @Schema(example = "Sala Tayrona") @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
            @Schema(example = "MEETING_ROOM") @NotNull(message = "El tipo es obligatorio") RoomType tipo,
            @Schema(example = "8") @NotNull(message = "La capacidad es obligatoria") @Min(value = 1, message = "La capacidad minima es 1") Integer capacidad,
            @Schema(example = "55000.00") @NotNull(message = "La tarifa por hora es obligatoria")
            @DecimalMin(value = "0.0", message = "La tarifa no puede ser negativa") BigDecimal tarifaHora,
            @Schema(description = "Por defecto true", example = "true") Boolean activa,
            @Schema(example = "1") @NotNull(message = "La sede es obligatoria") Long sedeId,
            @Schema(example = "[1, 2]") Set<Long> equipamientoIds) {
    }

    @Schema(name = "SalaResponse")
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
