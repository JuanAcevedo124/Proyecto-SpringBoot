package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.coworkhub.negocio.model.Equipment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO de equipamiento. */
public final class EquipamientoDto {

    private EquipamientoDto() {
    }

    @Schema(name = "EquipamientoRequest")
    public record Request(@Schema(example = "Proyector 4K") @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String nombre) {
    }

    @Schema(name = "EquipamientoResponse")
    public record Response(Long id, String nombre) {

        public static Response de(Equipment e) {
            return new Response(e.getId(), e.getName());
        }
    }
}
