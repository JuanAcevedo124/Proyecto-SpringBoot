package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.service.EquipamientoService;
import com.coworkhub.presentacion.dto.EquipamientoDto;
import com.coworkhub.config.AutorizacionRecursos;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipamientos")
public class EquipamientoController {

    private final EquipamientoService servicio;

    public EquipamientoController(EquipamientoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<EquipamientoDto.Response> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public EquipamientoDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipamientoDto.Response crear(@Valid @RequestBody EquipamientoDto.Request request) {
        return servicio.crear(request);
    }

    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PutMapping("/{id}")
    public EquipamientoDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody EquipamientoDto.Request request) {
        return servicio.actualizar(id, request);
    }

    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
