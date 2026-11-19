package com.coworkhub.negocio.service;

import com.coworkhub.presentacion.dto.ServicioAdicionalDto;

import java.util.List;

/** Casos de uso del catalogo de servicios adicionales (RF-01). */
public interface ServicioAdicionalService {

    List<ServicioAdicionalDto.Response> listar();

    ServicioAdicionalDto.Response obtener(Long id);

    ServicioAdicionalDto.Response crear(ServicioAdicionalDto.Request request);

    ServicioAdicionalDto.Response actualizar(Long id, ServicioAdicionalDto.Request request);

    void eliminar(Long id);
}
