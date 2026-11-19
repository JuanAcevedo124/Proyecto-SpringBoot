package com.coworkhub.presentacion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.coworkhub.negocio.service.EquipamientoService;
import com.coworkhub.presentacion.dto.EquipamientoDto;
import com.coworkhub.config.AutorizacionRecursos;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Equipamiento", description = "Catalogo de equipamiento (RF-01)")
@RestController
@RequestMapping("/api/equipamientos")
public class EquipamientoController {

    private final EquipamientoService servicio;

    public EquipamientoController(EquipamientoService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Lista el equipamiento", description = "Acceso: cualquier usuario autenticado.")
    @GetMapping
    public List<EquipamientoDto.Response> listar() {
        return servicio.listar();
    }

    @Operation(summary = "Consulta un equipamiento por id", description = "Acceso: cualquier usuario autenticado.")
    @GetMapping("/{id}")
    public EquipamientoDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Crea un equipamiento", description = "Acceso: solo ADMIN. Responde 201 con el recurso creado.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipamientoDto.Response crear(@Valid @RequestBody EquipamientoDto.Request request) {
        return servicio.crear(request);
    }

    @Operation(summary = "Actualiza un equipamiento", description = "Acceso: solo ADMIN.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PutMapping("/{id}")
    public EquipamientoDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody EquipamientoDto.Request request) {
        return servicio.actualizar(id, request);
    }

    @Operation(summary = "Elimina un equipamiento", description = "Acceso: solo ADMIN. Responde 200 sin cuerpo. RN-12: no se elimina si esta en uso.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
