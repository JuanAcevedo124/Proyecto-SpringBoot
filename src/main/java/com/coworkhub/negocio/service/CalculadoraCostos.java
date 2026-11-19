package com.coworkhub.negocio.service;

import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.Booking;
import com.coworkhub.negocio.model.BookingItem;
import com.coworkhub.negocio.model.MembershipPlan;
import com.coworkhub.negocio.model.Room;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bean con las reglas de dinero: costo de la reserva (RN-07) y cargo por cancelacion (RN-08).
 * No tiene estado ni depende de la base de datos, asi que se prueba con pruebas unitarias.
 */
@Component
public class CalculadoraCostos {

    private static final BigDecimal SESENTA = BigDecimal.valueOf(60);
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal CINCUENTA_POR_CIENTO = new BigDecimal("0.50");
    static final Duration SIN_CARGO_DESDE = Duration.ofHours(24);
    static final Duration CANCELABLE_HASTA = Duration.ofHours(2);

    /**
     * RN-07: las horas que el plan incluye en el mes no se cobran; las excedentes valen
     * hourlyRate x (1 - overageDiscount/100).
     *
     * @param minutosUsadosMes minutos que el miembro ya consumio del plan en el mes de la reserva
     * @param minutosReserva   duracion de la reserva nueva
     */
    public BigDecimal costoSala(Room sala, MembershipPlan plan, long minutosUsadosMes, long minutosReserva) {
        long minutosIncluidos = plan.getIncludedHoursPerMonth() * 60L;
        long minutosRestantes = Math.max(0, minutosIncluidos - minutosUsadosMes);
        long minutosExcedentes = Math.max(0, minutosReserva - minutosRestantes);
        if (minutosExcedentes == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal horasExcedentes = BigDecimal.valueOf(minutosExcedentes).divide(SESENTA, 4, RoundingMode.HALF_UP);
        BigDecimal factor = BigDecimal.ONE.subtract(BigDecimal.valueOf(plan.getOverageDiscount()).divide(CIEN, 4, RoundingMode.HALF_UP));
        return sala.getHourlyRate().multiply(horasExcedentes).multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    /** RN-07: los servicios suman quantity x appliedUnitPrice (el precio vigente al agregarlos). */
    public BigDecimal costoServicios(List<BookingItem> items) {
        return items.stream()
                .map(BookingItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Recalcula el costo de servicios y el total de una reserva (el costo de sala no cambia). */
    public void recalcularTotales(Booking reserva) {
        BigDecimal servicios = costoServicios(reserva.getItems());
        reserva.setServicesCost(servicios);
        reserva.setTotalCost(reserva.getRoomCost().add(servicios).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * RN-08: con 24 h o mas de anticipacion no hay cargo; entre 24 h y 2 h se cobra el 50 % del costo de la sala;
     * con menos de 2 h, o si ya empezo, no se puede cancelar. Los limites exactos (24 h y 2 h) pertenecen al tramo
     * mas favorable para el miembro.
     */
    public BigDecimal cargoCancelacion(Booking reserva, LocalDateTime ahora) {
        if (!ahora.isBefore(reserva.getStartTime())) {
            throw new ReglaNegocioException("RN-08", "La reserva ya empezo y no se puede cancelar");
        }
        Duration anticipacion = Duration.between(ahora, reserva.getStartTime());
        if (anticipacion.compareTo(SIN_CARGO_DESDE) >= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        if (anticipacion.compareTo(CANCELABLE_HASTA) >= 0) {
            return reserva.getRoomCost().multiply(CINCUENTA_POR_CIENTO).setScale(2, RoundingMode.HALF_UP);
        }
        throw new ReglaNegocioException("RN-08",
                "No se puede cancelar con menos de 2 horas de anticipacion");
    }
}
