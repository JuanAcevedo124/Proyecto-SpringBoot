package com.coworkhub.config;

import com.coworkhub.jwt.UsuarioAutenticado;
import com.coworkhub.negocio.service.ReservaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Verificaciones de "duenio del recurso" usadas en las expresiones @PreAuthorize (RN-13),
 * por ejemplo {@code @autorizacion.esDuenoReserva(#id)}.
 */
@Component("autorizacion")
public class AutorizacionRecursos {

    /** Expresiones reutilizables en los controladores. */
    public static final String PERSONAL = "hasAnyRole('ADMIN','RECEPTIONIST')";
    public static final String ADMIN = "hasRole('ADMIN')";

    private final ReservaService reservas;

    public AutorizacionRecursos(ReservaService reservas) {
        this.reservas = reservas;
    }

    /** true si el usuario autenticado es el miembro indicado. */
    public boolean esMiembroActual(Long miembroId) {
        return miembroId != null && miembroActual().map(miembroId::equals).orElse(false);
    }

    /**
     * true si la reserva pertenece al miembro autenticado. Si la reserva no existe devuelve true para que el
     * servicio responda 404 (no hay datos ajenos que proteger).
     */
    public boolean esDuenoReserva(Long reservaId) {
        Optional<Long> actual = miembroActual();
        if (actual.isEmpty()) {
            return false;
        }
        return reservas.miembroDeReserva(reservaId).map(actual.get()::equals).orElse(true);
    }

    private Optional<Long> miembroActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado u) {
            return Optional.ofNullable(u.miembroId());
        }
        return Optional.empty();
    }
}
