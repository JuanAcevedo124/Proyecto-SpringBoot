package com.coworkhub.negocio.service;

import com.coworkhub.presentacion.dto.SedeDto;

import java.util.List;

/** Casos de uso del catalogo de sedes (RF-01). */
public interface SedeService {

    List<SedeDto.Response> listar();

    SedeDto.Response obtener(Long id);

    SedeDto.Response crear(SedeDto.Request request);

    SedeDto.Response actualizar(Long id, SedeDto.Request request);

    void eliminar(Long id);
}
