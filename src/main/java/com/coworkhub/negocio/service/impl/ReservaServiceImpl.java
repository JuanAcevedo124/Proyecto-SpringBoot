package com.coworkhub.negocio.service.impl;

import com.coworkhub.negocio.excepcion.DatosInvalidosException;
import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.*;
import com.coworkhub.negocio.service.CalculadoraCostos;
import com.coworkhub.negocio.service.ContadorHoras;
import com.coworkhub.negocio.service.ReglasReserva;
import com.coworkhub.negocio.service.ReservaService;
import com.coworkhub.persistence.repository.AdditionalServiceRepository;
import com.coworkhub.persistence.repository.BookingRepository;
import com.coworkhub.persistence.repository.MemberRepository;
import com.coworkhub.persistence.repository.RoomRepository;
import com.coworkhub.presentacion.dto.ReservaDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReservaServiceImpl implements ReservaService {

    private final BookingRepository reservas;
    private final RoomRepository salas;
    private final MemberRepository miembros;
    private final AdditionalServiceRepository servicios;
    private final CalculadoraCostos calculadora;
    private final ContadorHoras contadorHoras;
    private final ReglasReserva reglas;
    private final Clock reloj;

    public ReservaServiceImpl(BookingRepository reservas, RoomRepository salas, MemberRepository miembros,
                              AdditionalServiceRepository servicios, CalculadoraCostos calculadora,
                              ContadorHoras contadorHoras, ReglasReserva reglas, Clock reloj) {
        this.reservas = reservas;
        this.salas = salas;
        this.miembros = miembros;
        this.servicios = servicios;
        this.calculadora = calculadora;
        this.contadorHoras = contadorHoras;
        this.reglas = reglas;
        this.reloj = reloj;
    }

    /**
     * RF-04. La primera sentencia bloquea la fila de la sala (SELECT ... FOR UPDATE): dos solicitudes simultaneas
     * para la misma sala se ejecutan una detras de otra, y la segunda ya ve la reserva de la primera (RN-01).
     * READ_COMMITTED garantiza que la consulta de cruce lea lo ultimo confirmado tras obtener el bloqueo.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaDto.Response crear(ReservaDto.CrearRequest r) {
        Room sala = salas.findByIdForUpdate(r.salaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sala", r.salaId()));
        Member miembro = miembros.findWithDetailsById(r.miembroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Miembro", r.miembroId()));
        LocalDateTime ahora = LocalDateTime.now(reloj);

        reglas.validarMiembro(miembro);                                        // RN-05
        reglas.validarSala(sala, r.asistentes());                              // RN-04
        reglas.validarDuracion(r.inicio(), r.fin());                           // RN-02
        reglas.validarHorario(sala.getLocation(), r.inicio(), r.fin(), ahora); // RN-03

        if (reservas.existeCruce(sala.getId(), BookingStatus.ACTIVOS, r.inicio(), r.fin())) {  // RN-01
            throw new ReglaNegocioException("RN-01", "La sala ya esta reservada en ese horario");
        }
        MembershipPlan plan = miembro.getPlan();
        long activas = reservas.countByMemberIdAndStatusInAndStartTimeAfter(miembro.getId(), BookingStatus.ACTIVOS, ahora);
        if (activas >= plan.getMaxActiveBookings()) {                          // RN-06
            throw new ReglaNegocioException("RN-06", "El miembro ya tiene " + activas
                    + " reservas activas, el maximo de su plan es " + plan.getMaxActiveBookings());
        }

        Booking reserva = new Booking();
        reserva.setRoom(sala);
        reserva.setMember(miembro);
        reserva.setStartTime(r.inicio());
        reserva.setEndTime(r.fin());
        reserva.setAttendees(r.asistentes());
        reserva.setStatus(BookingStatus.PENDING);
        agruparServicios(r.serviciosOVacio()).forEach((servicioId, cantidad) ->
                reserva.agregarItem(new BookingItem(buscarServicio(servicioId), cantidad)));

        long usadosMes = minutosUsadosEnElMes(miembro.getId(), r.inicio());   // RN-07
        reserva.setRoomCost(calculadora.costoSala(sala, plan, usadosMes, reserva.duracionMinutos()));
        calculadora.recalcularTotales(reserva);

        return ReservaDto.Response.de(reservas.save(reserva));
    }

    @Override
    public ReservaDto.Response obtener(Long id) {
        return ReservaDto.Response.de(buscar(id));
    }

    /** RF-05: un filtro por vez, en este orden de prioridad: miembro, sala + fecha, estado. Sin filtros, todas. */
    @Override
    public List<ReservaDto.Response> listar(Long miembroId, Long salaId, LocalDate fecha, BookingStatus estado) {
        List<Booking> lista;
        if (miembroId != null) {
            if (!miembros.existsById(miembroId)) {
                throw new RecursoNoEncontradoException("Miembro", miembroId);
            }
            lista = reservas.findByMemberIdOrderByStartTimeAsc(miembroId);
        } else if (salaId != null) {
            if (fecha == null) {
                throw new DatosInvalidosException("Para filtrar por sala tambien se debe indicar la fecha");
            }
            if (!salas.existsById(salaId)) {
                throw new RecursoNoEncontradoException("Sala", salaId);
            }
            lista = reservas.findByRoomIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
                    salaId, fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
        } else if (estado != null) {
            lista = reservas.findByStatusOrderByStartTimeAsc(estado);
        } else {
            lista = reservas.findAllByOrderByStartTimeAsc();
        }
        return lista.stream().map(ReservaDto.Response::de).toList();
    }

    /** RF-06 / RN-10: agrega un servicio (o suma cantidad si ya estaba) con el precio vigente, y recalcula. */
    @Override
    @Transactional
    public ReservaDto.Response agregarServicio(Long reservaId, ReservaDto.ServicioRequest r) {
        Booking reserva = buscar(reservaId);
        exigirPendiente(reserva);
        AdditionalService servicio = buscarServicio(r.servicioId());
        reserva.itemDeServicio(servicio.getId()).ifPresentOrElse(
                item -> item.setQuantity(item.getQuantity() + r.cantidad()),
                () -> reserva.agregarItem(new BookingItem(servicio, r.cantidad())));
        calculadora.recalcularTotales(reserva);
        return ReservaDto.Response.de(reservas.saveAndFlush(reserva));
    }

    /** RF-06 / RN-10: quita el servicio; orphanRemoval borra el detalle de la base. */
    @Override
    @Transactional
    public ReservaDto.Response quitarServicio(Long reservaId, Long servicioId) {
        Booking reserva = buscar(reservaId);
        exigirPendiente(reserva);
        if (!reserva.quitarItemDeServicio(servicioId)) {
            throw new RecursoNoEncontradoException("Servicio en la reserva", servicioId);
        }
        calculadora.recalcularTotales(reserva);
        return ReservaDto.Response.de(reservas.saveAndFlush(reserva));
    }

    @Override
    @Transactional
    public ReservaDto.Response confirmar(Long id) {
        Booking reserva = buscar(id);
        cambiarEstado(reserva, BookingStatus.CONFIRMED);
        return ReservaDto.Response.de(reserva);
    }

    /** RF-08: aplica RN-09 (transicion) y RN-08 (cargo) y devuelve la reserva con el cargo. */
    @Override
    @Transactional
    public ReservaDto.Response cancelar(Long id) {
        Booking reserva = buscar(id);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        exigirTransicion(reserva, BookingStatus.CANCELLED);
        reserva.setCancellationFee(calculadora.cargoCancelacion(reserva, ahora));
        reserva.setCancelledAt(ahora);
        reserva.setStatus(BookingStatus.CANCELLED);
        return ReservaDto.Response.de(reserva);
    }

    @Override
    @Transactional
    public ReservaDto.Response completar(Long id) {
        Booking reserva = buscar(id);
        cambiarEstado(reserva, BookingStatus.COMPLETED);
        return ReservaDto.Response.de(reserva);
    }

    @Override
    public Optional<Long> miembroDeReserva(Long reservaId) {
        return reservas.findMemberIdByBookingId(reservaId);
    }

    /* ------------------------------------------------------------------------------------------------ */

    private long minutosUsadosEnElMes(Long miembroId, LocalDateTime inicio) {
        YearMonth mes = YearMonth.from(inicio);
        List<Booking> delMes = reservas.findByMemberIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                miembroId, mes.atDay(1).atStartOfDay(), mes.plusMonths(1).atDay(1).atStartOfDay());
        return contadorHoras.minutosConsumidos(delMes);
    }

    /** Si el mismo servicio viene repetido en la solicitud, se suman las cantidades en un solo detalle. */
    private static Map<Long, Integer> agruparServicios(List<ReservaDto.ServicioRequest> pedidos) {
        Map<Long, Integer> agrupados = new LinkedHashMap<>();
        pedidos.forEach(p -> agrupados.merge(p.servicioId(), p.cantidad(), Integer::sum));
        return agrupados;
    }

    private void cambiarEstado(Booking reserva, BookingStatus destino) {
        exigirTransicion(reserva, destino);
        reserva.setStatus(destino);
    }

    /** RN-09: solo PENDING -> CONFIRMED -> COMPLETED y PENDING/CONFIRMED -> CANCELLED. */
    private static void exigirTransicion(Booking reserva, BookingStatus destino) {
        if (!reserva.getStatus().puedeCambiarA(destino)) {
            throw new ReglaNegocioException("RN-09",
                    "No se puede pasar una reserva de " + reserva.getStatus() + " a " + destino);
        }
    }

    /** RN-10: los servicios solo se cambian mientras la reserva esta PENDING. */
    private static void exigirPendiente(Booking reserva) {
        if (reserva.getStatus() != BookingStatus.PENDING) {
            throw new ReglaNegocioException("RN-10",
                    "Los servicios solo se pueden cambiar mientras la reserva esta PENDING (estado actual: "
                            + reserva.getStatus() + ")");
        }
    }

    private Booking buscar(Long id) {
        return reservas.findWithDetailsById(id).orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
    }

    private AdditionalService buscarServicio(Long id) {
        return servicios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Servicio adicional", id));
    }
}
