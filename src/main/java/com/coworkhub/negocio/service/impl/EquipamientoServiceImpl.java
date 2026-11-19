package com.coworkhub.negocio.service.impl;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.Equipment;
import com.coworkhub.negocio.service.EquipamientoService;
import com.coworkhub.persistence.repository.EquipmentRepository;
import com.coworkhub.persistence.repository.RoomRepository;
import com.coworkhub.presentacion.dto.EquipamientoDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EquipamientoServiceImpl implements EquipamientoService {

    private final EquipmentRepository equipos;
    private final RoomRepository salas;

    public EquipamientoServiceImpl(EquipmentRepository equipos, RoomRepository salas) {
        this.equipos = equipos;
        this.salas = salas;
    }

    @Override
    public List<EquipamientoDto.Response> listar() {
        return equipos.findAllByOrderByIdAsc().stream().map(EquipamientoDto.Response::de).toList();
    }

    @Override
    public EquipamientoDto.Response obtener(Long id) {
        return EquipamientoDto.Response.de(buscar(id));
    }

    @Override
    @Transactional
    public EquipamientoDto.Response crear(EquipamientoDto.Request request) {
        String nombre = request.nombre().trim();
        if (equipos.existsByNameIgnoreCase(nombre)) {
            throw duplicado(nombre);
        }
        Equipment equipo = new Equipment();
        equipo.setName(nombre);
        return EquipamientoDto.Response.de(equipos.save(equipo));
    }

    @Override
    @Transactional
    public EquipamientoDto.Response actualizar(Long id, EquipamientoDto.Request request) {
        Equipment equipo = buscar(id);
        String nombre = request.nombre().trim();
        if (equipos.existsByNameIgnoreCaseAndIdNot(nombre, id)) {
            throw duplicado(nombre);
        }
        equipo.setName(nombre);
        return EquipamientoDto.Response.de(equipo);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Equipment equipo = buscar(id);
        if (salas.existsByEquipmentId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar un equipamiento asignado a salas");
        }
        equipos.delete(equipo);
    }

    private Equipment buscar(Long id) {
        return equipos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Equipamiento", id));
    }

    private ReglaNegocioException duplicado(String nombre) {
        return new ReglaNegocioException("RN-11", "Ya existe un equipamiento con el nombre '" + nombre + "'");
    }
}
