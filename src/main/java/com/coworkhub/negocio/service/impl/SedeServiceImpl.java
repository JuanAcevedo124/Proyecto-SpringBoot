package com.coworkhub.negocio.service.impl;

import com.coworkhub.negocio.excepcion.DatosInvalidosException;
import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.Location;
import com.coworkhub.negocio.service.SedeService;
import com.coworkhub.persistence.repository.LocationRepository;
import com.coworkhub.persistence.repository.RoomRepository;
import com.coworkhub.presentacion.dto.SedeDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SedeServiceImpl implements SedeService {

    private final LocationRepository sedes;
    private final RoomRepository salas;

    public SedeServiceImpl(LocationRepository sedes, RoomRepository salas) {
        this.sedes = sedes;
        this.salas = salas;
    }

    @Override
    public List<SedeDto.Response> listar() {
        return sedes.findAllByOrderByIdAsc().stream().map(SedeDto.Response::de).toList();
    }

    @Override
    public SedeDto.Response obtener(Long id) {
        return SedeDto.Response.de(buscar(id));
    }

    @Override
    @Transactional
    public SedeDto.Response crear(SedeDto.Request request) {
        Location sede = new Location();
        aplicar(sede, request);
        return SedeDto.Response.de(sedes.save(sede));
    }

    @Override
    @Transactional
    public SedeDto.Response actualizar(Long id, SedeDto.Request request) {
        Location sede = buscar(id);
        aplicar(sede, request);
        return SedeDto.Response.de(sede);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Location sede = buscar(id);
        if (salas.existsByLocationId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar una sede que tiene salas");
        }
        sedes.delete(sede);
    }

    private Location buscar(Long id) {
        return sedes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sede", id));
    }

    private void aplicar(Location sede, SedeDto.Request r) {
        if (!r.horaApertura().isBefore(r.horaCierre())) {
            throw new DatosInvalidosException("La hora de apertura debe ser anterior a la de cierre");
        }
        sede.setName(r.nombre().trim());
        sede.setCity(r.ciudad().trim());
        sede.setAddress(r.direccion().trim());
        sede.setOpeningTime(r.horaApertura());
        sede.setClosingTime(r.horaCierre());
    }
}
