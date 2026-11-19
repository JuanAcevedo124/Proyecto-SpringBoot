package com.coworkhub.negocio.service;

import com.coworkhub.presentacion.dto.PlanDto;

import java.util.List;

/** Casos de uso del catalogo de planes de membresia (RF-01). */
public interface PlanService {

    List<PlanDto.Response> listar();

    PlanDto.Response obtener(Long id);

    PlanDto.Response crear(PlanDto.Request request);

    PlanDto.Response actualizar(Long id, PlanDto.Request request);

    void eliminar(Long id);
}
