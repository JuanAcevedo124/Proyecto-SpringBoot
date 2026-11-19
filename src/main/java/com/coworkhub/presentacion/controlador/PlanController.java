package com.coworkhub.presentacion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.coworkhub.negocio.service.PlanService;
import com.coworkhub.presentacion.dto.PlanDto;
import com.coworkhub.config.AutorizacionRecursos;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Planes", description = "Catalogo de planes de membresia (RF-01)")
@RestController
@RequestMapping("/api/planes")
public class PlanController {

    private final PlanService servicio;

    public PlanController(PlanService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Lista los planes", description = "Acceso: cualquier usuario autenticado.")
    @GetMapping
    public List<PlanDto.Response> listar() {
        return servicio.listar();
    }

    @Operation(summary = "Consulta un plan por id", description = "Acceso: cualquier usuario autenticado.")
    @GetMapping("/{id}")
    public PlanDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Crea un plan", description = "Acceso: solo ADMIN. Responde 201 con el recurso creado.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanDto.Response crear(@Valid @RequestBody PlanDto.Request request) {
        return servicio.crear(request);
    }

    @Operation(summary = "Actualiza un plan", description = "Acceso: solo ADMIN.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PutMapping("/{id}")
    public PlanDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody PlanDto.Request request) {
        return servicio.actualizar(id, request);
    }

    @Operation(summary = "Elimina un plan", description = "Acceso: solo ADMIN. Responde 200 sin cuerpo. RN-12: no se elimina si esta en uso.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
