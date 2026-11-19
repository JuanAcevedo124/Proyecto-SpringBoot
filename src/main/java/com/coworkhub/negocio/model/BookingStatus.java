package com.coworkhub.negocio.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados de una reserva y sus transiciones permitidas (RN-09):
 * PENDING -> CONFIRMED -> COMPLETED, o PENDING/CONFIRMED -> CANCELLED.
 */
public enum BookingStatus {
    PENDING, CONFIRMED, COMPLETED, CANCELLED;

    /** Estados que ocupan la sala (RN-01) y cuentan como reserva activa (RN-06). */
    public static final Set<BookingStatus> ACTIVOS = EnumSet.of(PENDING, CONFIRMED);

    public boolean puedeCambiarA(BookingStatus destino) {
        return switch (this) {
            case PENDING -> destino == CONFIRMED || destino == CANCELLED;
            case CONFIRMED -> destino == COMPLETED || destino == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
