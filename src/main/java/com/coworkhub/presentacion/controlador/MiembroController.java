package com.coworkhub.presentacion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.coworkhub.negocio.model.MemberStatus;
import com.coworkhub.negocio.service.MiembroService;
import com.coworkhub.presentacion.dto.MiembroDto;
import com.coworkhub.config.AutorizacionRecursos;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.YearMonth;
import java.util.List;

@Tag(name = "Miembros", description = "Clientes del coworking y su consumo (RF-02, RF-10)")
@RestController
@RequestMapping("/api/miembros")
@PreAuthorize(AutorizacionRecursos.PERSONAL)
public class MiembroController {

    private final MiembroService servicio;
    private final Clock reloj;

    public MiembroController(MiembroService servicio, Clock reloj) {
        this.servicio = servicio;
        this.reloj = reloj;
    }

    @Operation(summary = "Lista miembros", description = "Acceso: ADMIN, RECEPTIONIST. Filtros opcionales ?nombre= (contiene, sin distinguir mayusculas) y ?estado=ACTIVE|SUSPENDED.")
    @GetMapping
    public List<MiembroDto.Response> listar(@RequestParam(required = false) String nombre,
                                            @RequestParam(required = false) MemberStatus estado) {
        return servicio.listar(nombre, estado);
    }

    @Operation(summary = "Consulta un miembro", description = "Acceso: ADMIN, RECEPTIONIST, o el propio MEMBER.")
    @PreAuthorize(AutorizacionRecursos.PERSONAL + " or @autorizacion.esMiembroActual(#id)")
    @GetMapping("/{id}")
    public MiembroDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Registra un miembro y su usuario", description = "Acceso: ADMIN, RECEPTIONIST. Crea el Member y su UserAccount (rol MEMBER) en una sola transaccion. RN-11: documento, email y usuario unicos.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MiembroDto.Response crear(@Valid @RequestBody MiembroDto.CrearRequest request) {
        return servicio.crear(request);
    }

    @Operation(summary = "Actualiza nombre, email y plan", description = "Acceso: ADMIN, RECEPTIONIST.")
    @PutMapping("/{id}")
    public MiembroDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody MiembroDto.ActualizarRequest request) {
        return servicio.actualizar(id, request);
    }

    @Operation(summary = "Suspende un miembro", description = "Acceso: ADMIN, RECEPTIONIST. Un miembro suspendido no puede reservar (RN-05).")
    @PatchMapping("/{id}/suspender")
    public MiembroDto.Response suspender(@PathVariable Long id) {
        return servicio.suspender(id);
    }

    @Operation(summary = "Reactiva un miembro", description = "Acceso: ADMIN, RECEPTIONIST.")
    @PatchMapping("/{id}/activar")
    public MiembroDto.Response activar(@PathVariable Long id) {
        return servicio.activar(id);
    }

    @Operation(summary = "Elimina un miembro y su usuario", description = "Acceso: ADMIN, RECEPTIONIST. RN-12: no se elimina si tiene reservas.")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }

    /** RF-10. Sin ?mes= se usa el mes actual. */
    @Operation(summary = "Consumo mensual de horas", description = "Acceso: ADMIN, RECEPTIONIST, o el propio MEMBER. ?mes=yyyy-MM (por defecto, el mes actual). Horas incluidas, usadas, restantes y excedentes.")
    @PreAuthorize(AutorizacionRecursos.PERSONAL + " or @autorizacion.esMiembroActual(#id)")
    @GetMapping("/{id}/consumo")
    public MiembroDto.ConsumoResponse consumo(@PathVariable Long id,
                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return servicio.consumo(id, mes == null ? YearMonth.now(reloj) : mes);
    }
}
