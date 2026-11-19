package com.coworkhub.presentacion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.coworkhub.negocio.service.SalaService;
import com.coworkhub.presentacion.dto.SalaDto;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Tag(name = "Disponibilidad", description = "Salas libres (RF-03)")
@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadController {

    private final SalaService salas;

    public DisponibilidadController(SalaService salas) {
        this.salas = salas;
    }

    @Operation(summary = "Salas libres de una sede", description = "Acceso: autenticado. Salas activas sin reservas PENDING/CONFIRMED que se crucen con el rango. Filtros opcionales: capacidadMinima y equipamientoIds (la sala debe tener todos). Ejemplo: ?sedeId=2&fecha=2030-06-03&horaInicio=09:00&horaFin=11:00&capacidadMinima=4&equipamientoIds=1,3")
    @GetMapping
    public List<SalaDto.Response> disponibles(
            @RequestParam Long sedeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaInicio,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaFin,
            @RequestParam(required = false) @Min(1) Integer capacidadMinima,
            @RequestParam(required = false) Set<Long> equipamientoIds) {
        return salas.disponibles(sedeId, fecha, horaInicio, horaFin, capacidadMinima, equipamientoIds);
    }
}
