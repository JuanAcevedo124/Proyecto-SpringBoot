package com.coworkhub.presentacion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.coworkhub.negocio.model.BookingStatus;
import com.coworkhub.negocio.service.ReservaService;
import com.coworkhub.presentacion.dto.ReservaDto;
import com.coworkhub.config.AutorizacionRecursos;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Reservas", description = "Reservas de salas y sus servicios (RF-04 a RF-09)")
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService servicio;

    public ReservaController(ReservaService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Crea una reserva", description = "Acceso: ADMIN, RECEPTIONIST, o MEMBER solo a su nombre. Valida RN-01 a RN-06 y calcula el costo (RN-07). Responde 201 con el costo.")
    @PreAuthorize(AutorizacionRecursos.PERSONAL + " or @autorizacion.esMiembroActual(#request.miembroId())")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaDto.Response crear(@Valid @RequestBody ReservaDto.CrearRequest request) {
        return servicio.crear(request);
    }

    @Operation(summary = "Consulta una reserva", description = "Acceso: ADMIN, RECEPTIONIST, o el MEMBER duenio.")
    @PreAuthorize(AutorizacionRecursos.PERSONAL + " or @autorizacion.esDuenoReserva(#id)")
    @GetMapping("/{id}")
    public ReservaDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Lista reservas con un filtro", description = "Acceso: ADMIN, RECEPTIONIST; MEMBER solo con su propio ?miembroId=. Filtros: ?miembroId= | ?salaId=&fecha=yyyy-MM-dd | ?estado=.")
    @PreAuthorize(AutorizacionRecursos.PERSONAL + " or @autorizacion.esMiembroActual(#miembroId)")
    @GetMapping
    public List<ReservaDto.Response> listar(@RequestParam(required = false) Long miembroId,
                                            @RequestParam(required = false) Long salaId,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                            @RequestParam(required = false) BookingStatus estado) {
        return servicio.listar(miembroId, salaId, fecha, estado);
    }

    @Operation(summary = "Agrega un servicio a la reserva", description = "Acceso: ADMIN, RECEPTIONIST, o el MEMBER duenio. Solo con la reserva PENDING (RN-10). Guarda el precio vigente y recalcula el costo.")
    @PreAuthorize(AutorizacionRecursos.PERSONAL + " or @autorizacion.esDuenoReserva(#id)")
    @PostMapping("/{id}/servicios")
    public ReservaDto.Response agregarServicio(@PathVariable Long id, @Valid @RequestBody ReservaDto.ServicioRequest request) {
        return servicio.agregarServicio(id, request);
    }

    @Operation(summary = "Quita un servicio de la reserva", description = "Acceso: ADMIN, RECEPTIONIST, o el MEMBER duenio. Solo con la reserva PENDING (RN-10). El detalle se borra de la base (orphanRemoval).")
    @PreAuthorize(AutorizacionRecursos.PERSONAL + " or @autorizacion.esDuenoReserva(#id)")
    @DeleteMapping("/{id}/servicios/{servicioId}")
    public ReservaDto.Response quitarServicio(@PathVariable Long id, @PathVariable Long servicioId) {
        return servicio.quitarServicio(id, servicioId);
    }

    @Operation(summary = "Confirma una reserva", description = "Acceso: ADMIN, RECEPTIONIST. Solo PENDING -> CONFIRMED (RN-09).")
    @PreAuthorize(AutorizacionRecursos.PERSONAL)
    @PostMapping("/{id}/confirmar")
    public ReservaDto.Response confirmar(@PathVariable Long id) {
        return servicio.confirmar(id);
    }

    @Operation(summary = "Cancela una reserva", description = "Acceso: ADMIN, RECEPTIONIST, o el MEMBER duenio. RN-08: >= 24 h sin cargo y se devuelven las horas; entre 24 h y 2 h, 50 % del costo de sala; < 2 h o ya iniciada, 409.")
    @PreAuthorize(AutorizacionRecursos.PERSONAL + " or @autorizacion.esDuenoReserva(#id)")
    @PostMapping("/{id}/cancelar")
    public ReservaDto.Response cancelar(@PathVariable Long id) {
        return servicio.cancelar(id);
    }

    @Operation(summary = "Marca una reserva como completada", description = "Acceso: ADMIN, RECEPTIONIST. Solo CONFIRMED -> COMPLETED (RN-09).")
    @PreAuthorize(AutorizacionRecursos.PERSONAL)
    @PostMapping("/{id}/completar")
    public ReservaDto.Response completar(@PathVariable Long id) {
        return servicio.completar(id);
    }
}
