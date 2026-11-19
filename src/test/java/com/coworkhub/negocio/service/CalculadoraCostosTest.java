package com.coworkhub.negocio.service;

import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** RN-07 (costo) y RN-08 (cancelacion), incluidos los limites exactos de 24 h y 2 h. */
class CalculadoraCostosTest {

    private final CalculadoraCostos calculadora = new CalculadoraCostos();
    private static final LocalDateTime INICIO = LocalDateTime.of(2030, 6, 3, 10, 0);

    private static Room sala(String tarifa) {
        Room r = new Room();
        r.setHourlyRate(new BigDecimal(tarifa));
        return r;
    }

    private static MembershipPlan plan(int horas, int descuento) {
        MembershipPlan p = new MembershipPlan();
        p.setIncludedHoursPerMonth(horas);
        p.setOverageDiscount(descuento);
        p.setMaxActiveBookings(5);
        return p;
    }

    private static Booking reserva(String costoSala) {
        Booking b = new Booking();
        b.setStartTime(INICIO);
        b.setEndTime(INICIO.plusHours(2));
        b.setRoomCost(new BigDecimal(costoSala));
        return b;
    }

    @Test
    void horasIncluidasNoSeCobran() {
        assertThat(calculadora.costoSala(sala("50000"), plan(10, 20), 0, 120)).isEqualByComparingTo("0");
    }

    @Test
    void horasExcedentesSeCobranConDescuento() {
        // 10 h incluidas, 9 usadas -> de 2 h solo 1 h es excedente: 50000 x 1 x (1 - 0.20) = 40000
        assertThat(calculadora.costoSala(sala("50000"), plan(10, 20), 9 * 60, 120)).isEqualByComparingTo("40000.00");
    }

    @Test
    void mediaHoraExcedenteSinDescuento() {
        assertThat(calculadora.costoSala(sala("15000"), plan(5, 0), 5 * 60, 30)).isEqualByComparingTo("7500.00");
    }

    @Test
    void serviciosUsanElPrecioAplicadoYNoElActual() {
        AdditionalService cafe = new AdditionalService();
        cafe.setUnitPrice(new BigDecimal("8000"));
        Booking b = reserva("0");
        b.agregarItem(new BookingItem(cafe, 3));
        cafe.setUnitPrice(new BigDecimal("99999")); // cambiar el precio despues no altera la reserva
        calculadora.recalcularTotales(b);
        assertThat(b.getServicesCost()).isEqualByComparingTo("24000.00");
        assertThat(b.getTotalCost()).isEqualByComparingTo("24000.00");
    }

    @Test
    void cancelarCon24HorasExactasNoTieneCargo() {
        assertThat(calculadora.cargoCancelacion(reserva("40000"), INICIO.minusHours(24))).isEqualByComparingTo("0");
    }

    @Test
    void cancelarCon30HorasNoTieneCargo() {
        assertThat(calculadora.cargoCancelacion(reserva("40000"), INICIO.minusHours(30))).isEqualByComparingTo("0");
    }

    @Test
    void cancelarCasi24HorasAntesCobraElCincuentaPorCiento() {
        assertThat(calculadora.cargoCancelacion(reserva("40000"), INICIO.minusHours(24).plusSeconds(1)))
                .isEqualByComparingTo("20000.00");
    }

    @Test
    void cancelarCon2HorasExactasCobraElCincuentaPorCiento() {
        assertThat(calculadora.cargoCancelacion(reserva("40000"), INICIO.minusHours(2))).isEqualByComparingTo("20000.00");
    }

    @Test
    void cancelarConMenosDe2HorasSeRechaza() {
        assertThatThrownBy(() -> calculadora.cargoCancelacion(reserva("40000"), INICIO.minusHours(2).plusSeconds(1)))
                .isInstanceOf(ReglaNegocioException.class).hasFieldOrPropertyWithValue("codigo", "RN-08");
    }

    @Test
    void cancelarUnaReservaIniciadaSeRechaza() {
        assertThatThrownBy(() -> calculadora.cargoCancelacion(reserva("40000"), INICIO.plusMinutes(5)))
                .isInstanceOf(ReglaNegocioException.class).hasFieldOrPropertyWithValue("codigo", "RN-08");
    }
}
