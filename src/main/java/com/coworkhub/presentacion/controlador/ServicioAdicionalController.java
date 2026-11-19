package com.coworkhub.presentacion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.coworkhub.negocio.service.ServicioAdicionalService;
import com.coworkhub.presentacion.dto.ServicioAdicionalDto;
import com.coworkhub.config.AutorizacionRecursos;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Servicios adicionales", description = "Catalogo de servicios adicionales (RF-01)")
@RestController
@RequestMapping("/api/servicios-adicionales")
public class ServicioAdicionalController {

    private final ServicioAdicionalService servicio;

    public ServicioAdicionalController(ServicioAdicionalService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Lista los servicios adicionales", description = "Acceso: cualquier usuario autenticado.")
    @GetMapping
    public List<ServicioAdicionalDto.Response> listar() {
        return servicio.listar();
    }

    @Operation(summary = "Consulta un servicio adicional por id", description = "Acceso: cualquier usuario autenticado.")
    @GetMapping("/{id}")
    public ServicioAdicionalDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Crea un servicio adicional", description = "Acceso: solo ADMIN. Responde 201 con el recurso creado.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioAdicionalDto.Response crear(@Valid @RequestBody ServicioAdicionalDto.Request request) {
        return servicio.crear(request);
    }

    @Operation(summary = "Actualiza un servicio adicional", description = "Acceso: solo ADMIN. Cambiar el precio no altera las reservas existentes (RN-07).")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PutMapping("/{id}")
    public ServicioAdicionalDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody ServicioAdicionalDto.Request request) {
        return servicio.actualizar(id, request);
    }

    @Operation(summary = "Elimina un servicio adicional", description = "Acceso: solo ADMIN. Responde 200 sin cuerpo. RN-12: no se elimina si esta en uso.")
    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
