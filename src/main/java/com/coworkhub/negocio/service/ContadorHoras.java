package com.coworkhub.negocio.service;

import com.coworkhub.negocio.model.Booking;
import com.coworkhub.negocio.model.BookingStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;

/**
 * Cuenta las horas del plan que consumen las reservas de un miembro. La usan el calculo de costo (RN-07)
 * y el reporte de consumo (RF-10), asi la regla vive en un solo lugar.
 * <p>
 * Supuesto: una reserva consume horas si esta PENDING, CONFIRMED o COMPLETED. Una reserva CANCELLED
 * devuelve las horas solo si se cancelo con 24 h o mas de anticipacion (RN-08); si se cancelo tarde
 * (con cargo), las horas siguen contando como usadas.
 */
@Component
public class ContadorHoras {

    public long minutosConsumidos(Collection<Booking> reservas) {
        return reservas.stream()
                .filter(this::consumeHoras)
                .mapToLong(Booking::duracionMinutos)
                .sum();
    }

    public boolean consumeHoras(Booking reserva) {
        if (reserva.getStatus() != BookingStatus.CANCELLED) {
            return true;
        }
        return reserva.getCancelledAt() != null
                && Duration.between(reserva.getCancelledAt(), reserva.getStartTime())
                .compareTo(CalculadoraCostos.SIN_CARGO_DESDE) < 0;
    }
}
