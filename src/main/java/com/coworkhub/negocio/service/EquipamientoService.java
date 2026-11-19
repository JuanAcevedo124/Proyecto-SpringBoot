package com.coworkhub.negocio.service;

import com.coworkhub.presentacion.dto.EquipamientoDto;

import java.util.List;

/** Casos de uso del catalogo de equipamiento (RF-01). */
public interface EquipamientoService {

    List<EquipamientoDto.Response> listar();

    EquipamientoDto.Response obtener(Long id);

    EquipamientoDto.Response crear(EquipamientoDto.Request request);

    EquipamientoDto.Response actualizar(Long id, EquipamientoDto.Request request);

    void eliminar(Long id);
}
