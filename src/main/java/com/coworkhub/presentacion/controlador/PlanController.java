package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.service.PlanService;
import com.coworkhub.presentacion.dto.PlanDto;
import jakarta.validation.Valid;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanDto.Response crear(@Valid @RequestBody PlanDto.Request request) {
        return servicio.crear(request);
    }

    @PutMapping("/{id}")
    public PlanDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody PlanDto.Request request) {
        return servicio.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
