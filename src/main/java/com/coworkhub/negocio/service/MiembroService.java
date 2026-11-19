package com.coworkhub.negocio.service;

import com.coworkhub.negocio.model.MemberStatus;
import com.coworkhub.presentacion.dto.MiembroDto;

import java.time.YearMonth;
import java.util.List;

/** Casos de uso de miembros (RF-02) y consumo (RF-10). */
public interface MiembroService {

    List<MiembroDto.Response> listar(String nombre, MemberStatus estado);

    MiembroDto.Response obtener(Long id);

    MiembroDto.Response crear(MiembroDto.CrearRequest request);

    MiembroDto.Response actualizar(Long id, MiembroDto.ActualizarRequest request);

    MiembroDto.Response suspender(Long id);

    MiembroDto.Response activar(Long id);

    void eliminar(Long id);

    MiembroDto.ConsumoResponse consumo(Long id, YearMonth mes);
}
