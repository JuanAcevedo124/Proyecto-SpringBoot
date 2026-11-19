package com.coworkhub.negocio.service.impl;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.AdditionalService;
import com.coworkhub.negocio.service.ServicioAdicionalService;
import com.coworkhub.persistence.repository.AdditionalServiceRepository;
import com.coworkhub.persistence.repository.BookingItemRepository;
import com.coworkhub.presentacion.dto.ServicioAdicionalDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ServicioAdicionalServiceImpl implements ServicioAdicionalService {

    private final AdditionalServiceRepository servicios;
    private final BookingItemRepository detalles;

    public ServicioAdicionalServiceImpl(AdditionalServiceRepository servicios, BookingItemRepository detalles) {
        this.servicios = servicios;
        this.detalles = detalles;
    }

    @Override
    public List<ServicioAdicionalDto.Response> listar() {
        return servicios.findAllByOrderByIdAsc().stream().map(ServicioAdicionalDto.Response::de).toList();
    }

    @Override
    public ServicioAdicionalDto.Response obtener(Long id) {
        return ServicioAdicionalDto.Response.de(buscar(id));
    }

    @Override
    @Transactional
    public ServicioAdicionalDto.Response crear(ServicioAdicionalDto.Request request) {
        String nombre = request.nombre().trim();
        if (servicios.existsByNameIgnoreCase(nombre)) {
            throw duplicado(nombre);
        }
        AdditionalService servicio = new AdditionalService();
        servicio.setName(nombre);
        servicio.setUnitPrice(request.precioUnitario());
        return ServicioAdicionalDto.Response.de(servicios.save(servicio));
    }

    /** Cambiar el precio no altera las reservas existentes: sus detalles guardan appliedUnitPrice (RN-07). */
    @Override
    @Transactional
    public ServicioAdicionalDto.Response actualizar(Long id, ServicioAdicionalDto.Request request) {
        AdditionalService servicio = buscar(id);
        String nombre = request.nombre().trim();
        if (servicios.existsByNameIgnoreCaseAndIdNot(nombre, id)) {
            throw duplicado(nombre);
        }
        servicio.setName(nombre);
        servicio.setUnitPrice(request.precioUnitario());
        return ServicioAdicionalDto.Response.de(servicio);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        AdditionalService servicio = buscar(id);
        if (detalles.existsByServiceId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar un servicio usado en reservas");
        }
        servicios.delete(servicio);
    }

    private AdditionalService buscar(Long id) {
        return servicios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Servicio adicional", id));
    }

    private ReglaNegocioException duplicado(String nombre) {
        return new ReglaNegocioException("RN-11", "Ya existe un servicio adicional con el nombre '" + nombre + "'");
    }
}
