package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.service.SedeService;
import com.coworkhub.presentacion.dto.SedeDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sedes")
public class SedeController {

    private final SedeService servicio;

    public SedeController(SedeService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<SedeDto.Response> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public SedeDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SedeDto.Response crear(@Valid @RequestBody SedeDto.Request request) {
        return servicio.crear(request);
    }

    @PutMapping("/{id}")
    public SedeDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody SedeDto.Request request) {
        return servicio.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
