package com.coworkhub.negocio.service;

import com.coworkhub.presentacion.dto.SalaDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/** Casos de uso de salas (RF-01) y disponibilidad (RF-03). */
public interface SalaService {

    List<SalaDto.Response> listar(Long sedeId);

    SalaDto.Response obtener(Long id);

    SalaDto.Response crear(SalaDto.Request request);

    SalaDto.Response actualizar(Long id, SalaDto.Request request);

    void eliminar(Long id);

    List<SalaDto.Response> disponibles(Long sedeId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                       Integer capacidadMinima, Set<Long> equipamientoIds);
}
