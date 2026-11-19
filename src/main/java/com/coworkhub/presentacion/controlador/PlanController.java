package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.service.PlanService;
import com.coworkhub.presentacion.dto.PlanDto;
import com.coworkhub.config.AutorizacionRecursos;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planes")
public class PlanController {

    private final PlanService servicio;

    public PlanController(PlanService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<PlanDto.Response> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public PlanDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanDto.Response crear(@Valid @RequestBody PlanDto.Request request) {
        return servicio.crear(request);
    }

    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @PutMapping("/{id}")
    public PlanDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody PlanDto.Request request) {
        return servicio.actualizar(id, request);
    }

    @PreAuthorize(AutorizacionRecursos.ADMIN)
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
