package com.coworkhub.negocio.service.impl;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.MembershipPlan;
import com.coworkhub.negocio.service.PlanService;
import com.coworkhub.persistence.repository.MemberRepository;
import com.coworkhub.persistence.repository.MembershipPlanRepository;
import com.coworkhub.presentacion.dto.PlanDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlanServiceImpl implements PlanService {

    private final MembershipPlanRepository planes;
    private final MemberRepository miembros;

    public PlanServiceImpl(MembershipPlanRepository planes, MemberRepository miembros) {
        this.planes = planes;
        this.miembros = miembros;
    }

    @Override
    public List<PlanDto.Response> listar() {
        return planes.findAllByOrderByIdAsc().stream().map(PlanDto.Response::de).toList();
    }

    @Override
    public PlanDto.Response obtener(Long id) {
        return PlanDto.Response.de(buscar(id));
    }

    @Override
    @Transactional
    public PlanDto.Response crear(PlanDto.Request request) {
        String nombre = request.nombre().trim();
        if (planes.existsByNameIgnoreCase(nombre)) {
            throw duplicado(nombre);
        }
        MembershipPlan plan = new MembershipPlan();
        aplicar(plan, nombre, request);
        return PlanDto.Response.de(planes.save(plan));
    }

    @Override
    @Transactional
    public PlanDto.Response actualizar(Long id, PlanDto.Request request) {
        MembershipPlan plan = buscar(id);
        String nombre = request.nombre().trim();
        if (planes.existsByNameIgnoreCaseAndIdNot(nombre, id)) {
            throw duplicado(nombre);
        }
        aplicar(plan, nombre, request);
        return PlanDto.Response.de(plan);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        MembershipPlan plan = buscar(id);
        if (miembros.existsByPlanId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar un plan que tiene miembros");
        }
        planes.delete(plan);
    }

    private MembershipPlan buscar(Long id) {
        return planes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Plan", id));
    }

    private void aplicar(MembershipPlan plan, String nombre, PlanDto.Request r) {
        plan.setName(nombre);
        plan.setIncludedHoursPerMonth(r.horasIncluidasMes());
        plan.setOverageDiscount(r.descuentoExcedente());
        plan.setMaxActiveBookings(r.maxReservasActivas());
    }

    private ReglaNegocioException duplicado(String nombre) {
        return new ReglaNegocioException("RN-11", "Ya existe un plan con el nombre '" + nombre + "'");
    }
}
