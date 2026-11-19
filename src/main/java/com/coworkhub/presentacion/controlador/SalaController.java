package com.coworkhub.presentacion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.coworkhub.negocio.service.SalaService;
import com.coworkhub.presentacion.dto.SalaDto;
import com.coworkhub.config.AutorizacionRecursos;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Salas", description = "Catalogo de salas con su equipamiento (RF-01)")
@RestController
@RequestMapping("/api/salas")
public class SalaController {

    private final SalaService servicio;

    public SalaController(SalaService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Lista las salas", description = "Acceso: autenticado. Filtro opcional ?sedeId=.")
    @GetMapping
    public List<SalaDto.Response> listar(@RequestParam(required = false) Long sedeId) {
        return servicio.listar(sedeId);
    }

    @Operation(summary = "Consulta una sala por id", description = "Acceso: cualquier usuario autenticado.")
    @GetMapping("/{id}")
    public SalaDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Crea una sala", description = "Acceso: solo ADMIN. Responde 201 con el recurso creado.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalaDto.Response crear(@Valid @RequestBody SalaDto.Request request) {
        return servicio.crear(request);
    }

    @Operation(summary = "Actualiza una sala", description = "Acceso: solo ADMIN.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PutMapping("/{id}")
    public SalaDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody SalaDto.Request request) {
        return servicio.actualizar(id, request);
    }

    @Operation(summary = "Elimina una sala", description = "Acceso: solo ADMIN. Responde 200 sin cuerpo. RN-12: no se elimina si esta en uso.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
