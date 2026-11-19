package com.coworkhub.negocio.service;

import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** RN-02, RN-03, RN-04, RN-05 y RN-09 con una hora "actual" fija. */
class ReglasReservaTest {

    private final ReglasReserva reglas = new ReglasReserva();
    private static final LocalDateTime AHORA = LocalDateTime.of(2030, 6, 3, 8, 0);
    private static final LocalDateTime MANANA_10 = LocalDateTime.of(2030, 6, 4, 10, 0);

    private static Location sede() {
        Location l = new Location();
        l.setOpeningTime(LocalTime.of(7, 0));
        l.setClosingTime(LocalTime.of(21, 0));
        return l;
    }

    private static void esperaRegla(String codigo, Runnable r) {
        assertThatThrownBy(r::run).isInstanceOf(ReglaNegocioException.class)
                .hasFieldOrPropertyWithValue("codigo", codigo);
    }

    @Test
    void rn02DuracionValida() {
        assertThatCode(() -> reglas.validarDuracion(MANANA_10, MANANA_10.plusMinutes(30))).doesNotThrowAnyException();
        assertThatCode(() -> reglas.validarDuracion(MANANA_10, MANANA_10.plusHours(8))).doesNotThrowAnyException();
    }

    @Test
    void rn02Rechaza45MinutosMasDe8HorasYCruzarMedianoche() {
        esperaRegla("RN-02", () -> reglas.validarDuracion(MANANA_10, MANANA_10.plusMinutes(45)));
        esperaRegla("RN-02", () -> reglas.validarDuracion(MANANA_10, MANANA_10.plusHours(8).plusMinutes(30)));
        LocalDateTime noche = LocalDateTime.of(2030, 6, 4, 23, 0);
        esperaRegla("RN-02", () -> reglas.validarDuracion(noche, noche.plusHours(2)));
        esperaRegla("RN-02", () -> reglas.validarDuracion(MANANA_10, MANANA_10));
    }

    @Test
    void rn03RechazaFueraDeHorarioPasadoYPocaAnticipacion() {
        LocalDateTime temprano = LocalDateTime.of(2030, 6, 4, 6, 0);
        esperaRegla("RN-03", () -> reglas.validarHorario(sede(), temprano, temprano.plusHours(1), AHORA));
        esperaRegla("RN-03", () -> reglas.validarHorario(sede(), AHORA.minusHours(1), AHORA, AHORA));
        esperaRegla("RN-03", () -> reglas.validarHorario(sede(), AHORA.plusMinutes(30), AHORA.plusMinutes(90), AHORA));
        assertThatCode(() -> reglas.validarHorario(sede(), AHORA.plusHours(1), AHORA.plusHours(2), AHORA))
                .doesNotThrowAnyException();
    }

    @Test
    void rn04SalaInactivaYCapacidad() {
        Room sala = new Room();
        sala.setCapacity(4);
        sala.setActive(true);
        esperaRegla("RN-04", () -> reglas.validarSala(sala, 5));
        assertThatCode(() -> reglas.validarSala(sala, 4)).doesNotThrowAnyException();
        sala.setActive(false);
        esperaRegla("RN-04", () -> reglas.validarSala(sala, 1));
    }

    @Test
    void rn05MiembroSuspendidoNoReserva() {
        Member m = new Member();
        m.setStatus(MemberStatus.SUSPENDED);
        esperaRegla("RN-05", () -> reglas.validarMiembro(m));
    }

    @Test
    void rn09TransicionesPermitidas() {
        org.assertj.core.api.Assertions.assertThat(BookingStatus.PENDING.puedeCambiarA(BookingStatus.CONFIRMED)).isTrue();
        org.assertj.core.api.Assertions.assertThat(BookingStatus.CONFIRMED.puedeCambiarA(BookingStatus.COMPLETED)).isTrue();
        org.assertj.core.api.Assertions.assertThat(BookingStatus.PENDING.puedeCambiarA(BookingStatus.CANCELLED)).isTrue();
        org.assertj.core.api.Assertions.assertThat(BookingStatus.CONFIRMED.puedeCambiarA(BookingStatus.CANCELLED)).isTrue();
        org.assertj.core.api.Assertions.assertThat(BookingStatus.PENDING.puedeCambiarA(BookingStatus.COMPLETED)).isFalse();
        org.assertj.core.api.Assertions.assertThat(BookingStatus.CANCELLED.puedeCambiarA(BookingStatus.CONFIRMED)).isFalse();
        org.assertj.core.api.Assertions.assertThat(BookingStatus.COMPLETED.puedeCambiarA(BookingStatus.CANCELLED)).isFalse();
    }

    @Test
    void contadorDevuelveHorasSoloConCancelacionAnticipada() {
        ContadorHoras contador = new ContadorHoras();
        Booking temprana = new Booking();
        temprana.setStartTime(MANANA_10);
        temprana.setEndTime(MANANA_10.plusHours(2));
        temprana.setStatus(BookingStatus.CANCELLED);
        temprana.setCancelledAt(MANANA_10.minusHours(30));
        Booking tardia = new Booking();
        tardia.setStartTime(MANANA_10);
        tardia.setEndTime(MANANA_10.plusHours(1));
        tardia.setStatus(BookingStatus.CANCELLED);
        tardia.setCancelledAt(MANANA_10.minusHours(5));
        org.assertj.core.api.Assertions.assertThat(contador.minutosConsumidos(java.util.List.of(temprana, tardia))).isEqualTo(60);
    }
}
