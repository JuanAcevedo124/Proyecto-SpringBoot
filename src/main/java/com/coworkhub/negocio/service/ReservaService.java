package com.coworkhub.negocio.service;

import com.coworkhub.negocio.model.BookingStatus;
import com.coworkhub.presentacion.dto.ReservaDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Casos de uso de reservas (RF-04 a RF-09). */
public interface ReservaService {

    ReservaDto.Response crear(ReservaDto.CrearRequest request);

    ReservaDto.Response obtener(Long id);

    List<ReservaDto.Response> listar(Long miembroId, Long salaId, LocalDate fecha, BookingStatus estado);

    ReservaDto.Response agregarServicio(Long reservaId, ReservaDto.ServicioRequest request);

    ReservaDto.Response quitarServicio(Long reservaId, Long servicioId);

    ReservaDto.Response confirmar(Long id);

    ReservaDto.Response cancelar(Long id);

    ReservaDto.Response completar(Long id);

    /** Id del miembro duenio de la reserva, o vacio si la reserva no existe. Lo usa la verificacion de duenio. */
    Optional<Long> miembroDeReserva(Long reservaId);
}
