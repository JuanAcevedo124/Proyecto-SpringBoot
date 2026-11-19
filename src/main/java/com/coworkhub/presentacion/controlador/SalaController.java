package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.service.SalaService;
import com.coworkhub.presentacion.dto.SalaDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salas")
public class SalaController {

    private final SalaService servicio;

    public SalaController(SalaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<SalaDto.Response> listar(@RequestParam(required = false) Long sedeId) {
        return servicio.listar(sedeId);
    }

    @GetMapping("/{id}")
    public SalaDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalaDto.Response crear(@Valid @RequestBody SalaDto.Request request) {
        return servicio.crear(request);
    }

    @PutMapping("/{id}")
    public SalaDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody SalaDto.Request request) {
        return servicio.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
