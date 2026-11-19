package com.coworkhub.presentacion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.coworkhub.negocio.service.SedeService;
import com.coworkhub.presentacion.dto.SedeDto;
import com.coworkhub.config.AutorizacionRecursos;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Sedes", description = "Catalogo de sedes (RF-01)")
@RestController
@RequestMapping("/api/sedes")
public class SedeController {

    private final SedeService servicio;

    public SedeController(SedeService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Lista las sedes", description = "Acceso: cualquier usuario autenticado.")
    @GetMapping
    public List<SedeDto.Response> listar() {
        return servicio.listar();
    }

    @Operation(summary = "Consulta una sede por id", description = "Acceso: cualquier usuario autenticado.")
    @GetMapping("/{id}")
    public SedeDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Crea una sede", description = "Acceso: solo ADMIN. Responde 201 con el recurso creado.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SedeDto.Response crear(@Valid @RequestBody SedeDto.Request request) {
        return servicio.crear(request);
    }

    @Operation(summary = "Actualiza una sede", description = "Acceso: solo ADMIN.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PutMapping("/{id}")
    public SedeDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody SedeDto.Request request) {
        return servicio.actualizar(id, request);
    }

    @Operation(summary = "Elimina una sede", description = "Acceso: solo ADMIN. Responde 200 sin cuerpo. RN-12: no se elimina si esta en uso.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
