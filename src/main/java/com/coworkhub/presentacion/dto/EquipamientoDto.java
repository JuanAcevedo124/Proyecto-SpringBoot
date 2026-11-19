package com.coworkhub.presentacion.dto;

import com.coworkhub.negocio.model.Equipment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO de equipamiento. */
public final class EquipamientoDto {

    private EquipamientoDto() {
    }

    public record Request(@NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String nombre) {
    }

    public record Response(Long id, String nombre) {

        public static Response de(Equipment e) {
            return new Response(e.getId(), e.getName());
        }
    }
}
