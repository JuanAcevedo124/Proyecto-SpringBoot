package com.coworkhub.presentacion.controlador;

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

@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadController {

    private final SalaService salas;

    public DisponibilidadController(SalaService salas) {
        this.salas = salas;
    }

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
