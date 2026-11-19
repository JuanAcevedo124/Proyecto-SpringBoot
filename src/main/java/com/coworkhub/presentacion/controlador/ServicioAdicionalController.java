package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.service.ServicioAdicionalService;
import com.coworkhub.presentacion.dto.ServicioAdicionalDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios-adicionales")
public class ServicioAdicionalController {

    private final ServicioAdicionalService servicio;

    public ServicioAdicionalController(ServicioAdicionalService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ServicioAdicionalDto.Response> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public ServicioAdicionalDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioAdicionalDto.Response crear(@Valid @RequestBody ServicioAdicionalDto.Request request) {
        return servicio.crear(request);
    }

    @PutMapping("/{id}")
    public ServicioAdicionalDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody ServicioAdicionalDto.Request request) {
        return servicio.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
