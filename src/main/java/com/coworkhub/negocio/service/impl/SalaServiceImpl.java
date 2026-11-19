package com.coworkhub.negocio.service.impl;

import com.coworkhub.negocio.excepcion.DatosInvalidosException;
import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.BookingStatus;
import com.coworkhub.negocio.model.Equipment;
import com.coworkhub.negocio.model.Location;
import com.coworkhub.negocio.model.Room;
import com.coworkhub.negocio.service.SalaService;
import com.coworkhub.persistence.repository.BookingRepository;
import com.coworkhub.persistence.repository.EquipmentRepository;
import com.coworkhub.persistence.repository.LocationRepository;
import com.coworkhub.persistence.repository.RoomRepository;
import com.coworkhub.presentacion.dto.SalaDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SalaServiceImpl implements SalaService {

    private final RoomRepository salas;
    private final LocationRepository sedes;
    private final EquipmentRepository equipos;
    private final BookingRepository reservas;

    public SalaServiceImpl(RoomRepository salas, LocationRepository sedes, EquipmentRepository equipos,
                           BookingRepository reservas) {
        this.salas = salas;
        this.sedes = sedes;
        this.equipos = equipos;
        this.reservas = reservas;
    }

    @Override
    public List<SalaDto.Response> listar(Long sedeId) {
        List<Room> lista = sedeId == null ? salas.findAllByOrderByIdAsc() : salas.findByLocationIdOrderByIdAsc(sedeId);
        return lista.stream().map(SalaDto.Response::de).toList();
    }

    @Override
    public SalaDto.Response obtener(Long id) {
        return SalaDto.Response.de(buscar(id));
    }

    @Override
    @Transactional
    public SalaDto.Response crear(SalaDto.Request request) {
        Location sede = buscarSede(request.sedeId());
        String nombre = request.nombre().trim();
        if (salas.existsByLocationIdAndNameIgnoreCase(sede.getId(), nombre)) {
            throw duplicado(nombre);
        }
        Room sala = new Room();
        aplicar(sala, sede, nombre, request);
        return SalaDto.Response.de(salas.save(sala));
    }

    @Override
    @Transactional
    public SalaDto.Response actualizar(Long id, SalaDto.Request request) {
        Room sala = buscar(id);
        Location sede = buscarSede(request.sedeId());
        String nombre = request.nombre().trim();
        if (salas.existsByLocationIdAndNameIgnoreCaseAndIdNot(sede.getId(), nombre, id)) {
            throw duplicado(nombre);
        }
        aplicar(sala, sede, nombre, request);
        return SalaDto.Response.de(sala);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Room sala = buscar(id);
        if (reservas.existsByRoomId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar una sala que tiene reservas");
        }
        salas.delete(sala);
    }

    /** RF-03: salas activas de la sede, sin reservas activas que se crucen con el rango y con los filtros pedidos. */
    @Override
    public List<SalaDto.Response> disponibles(Long sedeId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                              Integer capacidadMinima, Set<Long> equipamientoIds) {
        Location sede = buscarSede(sedeId);
        if (!horaInicio.isBefore(horaFin)) {
            throw new DatosInvalidosException("La hora de inicio debe ser anterior a la hora de fin");
        }
        if (horaInicio.isBefore(sede.getOpeningTime()) || horaFin.isAfter(sede.getClosingTime())) {
            return List.of();
        }
        Set<Long> ocupadas = new HashSet<>(reservas.salasOcupadas(sedeId, BookingStatus.ACTIVOS,
                fecha.atTime(horaInicio), fecha.atTime(horaFin)));
        Set<Long> equiposPedidos = equipamientoIds == null ? Set.of() : equipamientoIds;
        int capacidad = capacidadMinima == null ? 1 : capacidadMinima;

        return salas.findByLocationIdAndActiveTrueAndCapacityGreaterThanEqualOrderByIdAsc(sedeId, capacidad).stream()
                .filter(s -> !ocupadas.contains(s.getId()))
                .filter(s -> s.getEquipment().stream().map(Equipment::getId).collect(Collectors.toSet())
                        .containsAll(equiposPedidos))
                .map(SalaDto.Response::de)
                .toList();
    }

    private Room buscar(Long id) {
        return salas.findWithDetailsById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sala", id));
    }

    private Location buscarSede(Long id) {
        return sedes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sede", id));
    }

    private void aplicar(Room sala, Location sede, String nombre, SalaDto.Request r) {
        Set<Long> ids = r.equipamientoIds() == null ? Set.of() : r.equipamientoIds();
        List<Equipment> encontrados = equipos.findAllById(ids);
        if (encontrados.size() != ids.size()) {
            Set<Long> existentes = encontrados.stream().map(Equipment::getId).collect(Collectors.toSet());
            Long faltante = ids.stream().filter(id -> !existentes.contains(id)).findFirst().orElse(null);
            throw new RecursoNoEncontradoException("Equipamiento", faltante);
        }
        sala.setName(nombre);
        sala.setType(r.tipo());
        sala.setCapacity(r.capacidad());
        sala.setHourlyRate(r.tarifaHora());
        sala.setActive(r.activa() == null || r.activa());
        sala.setLocation(sede);
        sala.setEquipment(new HashSet<>(encontrados));
    }

    private ReglaNegocioException duplicado(String nombre) {
        return new ReglaNegocioException("RN-11", "Ya existe una sala llamada '" + nombre + "' en esa sede");
    }
}
