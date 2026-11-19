package com.coworkhub.negocio.service;

import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.Location;
import com.coworkhub.negocio.model.Member;
import com.coworkhub.negocio.model.MemberStatus;
import com.coworkhub.negocio.model.Room;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/** Validaciones de una reserva nueva que no dependen de la base de datos: RN-02, RN-03, RN-04 y RN-05. */
@Component
public class ReglasReserva {

    static final long DURACION_MINIMA = 30;
    static final long DURACION_MAXIMA = 8 * 60;
    static final Duration ANTICIPACION_MINIMA = Duration.ofHours(1);

    /** RN-02: entre 30 min y 8 h, en multiplos de 30 min, empieza y termina el mismo dia. */
    public void validarDuracion(LocalDateTime inicio, LocalDateTime fin) {
        if (!fin.isAfter(inicio)) {
            throw new ReglaNegocioException("RN-02", "La hora de fin debe ser posterior a la de inicio");
        }
        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new ReglaNegocioException("RN-02", "La reserva debe empezar y terminar el mismo dia");
        }
        long minutos = Duration.between(inicio, fin).toMinutes();
        if (minutos < DURACION_MINIMA || minutos > DURACION_MAXIMA || minutos % 30 != 0) {
            throw new ReglaNegocioException("RN-02",
                    "La reserva debe durar entre 30 minutos y 8 horas, en multiplos de 30 minutos");
        }
    }

    /** RN-03: dentro del horario de la sede, futura y con al menos 1 h de anticipacion. */
    public void validarHorario(Location sede, LocalDateTime inicio, LocalDateTime fin, LocalDateTime ahora) {
        if (!inicio.isAfter(ahora)) {
            throw new ReglaNegocioException("RN-03", "La reserva debe ser en el futuro");
        }
        if (Duration.between(ahora, inicio).compareTo(ANTICIPACION_MINIMA) < 0) {
            throw new ReglaNegocioException("RN-03", "La reserva debe hacerse con al menos 1 hora de anticipacion");
        }
        if (inicio.toLocalTime().isBefore(sede.getOpeningTime()) || fin.toLocalTime().isAfter(sede.getClosingTime())) {
            throw new ReglaNegocioException("RN-03", "La reserva esta fuera del horario de la sede ("
                    + sede.getOpeningTime() + " - " + sede.getClosingTime() + ")");
        }
    }

    /** RN-04: la sala debe estar activa y los asistentes no pueden superar su capacidad. */
    public void validarSala(Room sala, int asistentes) {
        if (!sala.isActive()) {
            throw new ReglaNegocioException("RN-04", "La sala no esta activa");
        }
        if (asistentes > sala.getCapacity()) {
            throw new ReglaNegocioException("RN-04",
                    "Los asistentes (" + asistentes + ") superan la capacidad de la sala (" + sala.getCapacity() + ")");
        }
    }

    /** RN-05: solo reserva un miembro ACTIVE. */
    public void validarMiembro(Member miembro) {
        if (miembro.getStatus() != MemberStatus.ACTIVE) {
            throw new ReglaNegocioException("RN-05", "El miembro esta suspendido y no puede reservar");
        }
    }
}
