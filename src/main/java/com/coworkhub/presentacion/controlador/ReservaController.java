package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.model.BookingStatus;
import com.coworkhub.negocio.service.ReservaService;
import com.coworkhub.presentacion.dto.ReservaDto;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService servicio;

    public ReservaController(ReservaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaDto.Response crear(@Valid @RequestBody ReservaDto.CrearRequest request) {
        return servicio.crear(request);
    }

    @GetMapping("/{id}")
    public ReservaDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @GetMapping
    public List<ReservaDto.Response> listar(@RequestParam(required = false) Long miembroId,
                                            @RequestParam(required = false) Long salaId,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                            @RequestParam(required = false) BookingStatus estado) {
        return servicio.listar(miembroId, salaId, fecha, estado);
    }

    @PostMapping("/{id}/servicios")
    public ReservaDto.Response agregarServicio(@PathVariable Long id, @Valid @RequestBody ReservaDto.ServicioRequest request) {
        return servicio.agregarServicio(id, request);
    }

    @DeleteMapping("/{id}/servicios/{servicioId}")
    public ReservaDto.Response quitarServicio(@PathVariable Long id, @PathVariable Long servicioId) {
        return servicio.quitarServicio(id, servicioId);
    }

    @PostMapping("/{id}/confirmar")
    public ReservaDto.Response confirmar(@PathVariable Long id) {
        return servicio.confirmar(id);
    }

    @PostMapping("/{id}/cancelar")
    public ReservaDto.Response cancelar(@PathVariable Long id) {
        return servicio.cancelar(id);
    }

    @PostMapping("/{id}/completar")
    public ReservaDto.Response completar(@PathVariable Long id) {
        return servicio.completar(id);
    }
}
