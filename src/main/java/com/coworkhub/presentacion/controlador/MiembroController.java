package com.coworkhub.presentacion.controlador;

import com.coworkhub.negocio.model.MemberStatus;
import com.coworkhub.negocio.service.MiembroService;
import com.coworkhub.presentacion.dto.MiembroDto;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/miembros")
public class MiembroController {

    private final MiembroService servicio;
    private final Clock reloj;

    public MiembroController(MiembroService servicio, Clock reloj) {
        this.servicio = servicio;
        this.reloj = reloj;
    }

    @GetMapping
    public List<MiembroDto.Response> listar(@RequestParam(required = false) String nombre,
                                            @RequestParam(required = false) MemberStatus estado) {
        return servicio.listar(nombre, estado);
    }

    @GetMapping("/{id}")
    public MiembroDto.Response obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MiembroDto.Response crear(@Valid @RequestBody MiembroDto.CrearRequest request) {
        return servicio.crear(request);
    }

    @PutMapping("/{id}")
    public MiembroDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody MiembroDto.ActualizarRequest request) {
        return servicio.actualizar(id, request);
    }

    @PatchMapping("/{id}/suspender")
    public MiembroDto.Response suspender(@PathVariable Long id) {
        return servicio.suspender(id);
    }

    @PatchMapping("/{id}/activar")
    public MiembroDto.Response activar(@PathVariable Long id) {
        return servicio.activar(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }

    /** RF-10. Sin ?mes= se usa el mes actual. */
    @GetMapping("/{id}/consumo")
    public MiembroDto.ConsumoResponse consumo(@PathVariable Long id,
                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return servicio.consumo(id, mes == null ? YearMonth.now(reloj) : mes);
    }
}
