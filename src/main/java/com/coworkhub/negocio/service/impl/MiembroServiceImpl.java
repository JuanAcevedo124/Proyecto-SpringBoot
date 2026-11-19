package com.coworkhub.negocio.service.impl;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.*;
import com.coworkhub.negocio.service.ContadorHoras;
import com.coworkhub.negocio.service.MiembroService;
import com.coworkhub.persistence.repository.BookingRepository;
import com.coworkhub.persistence.repository.MemberRepository;
import com.coworkhub.persistence.repository.MembershipPlanRepository;
import com.coworkhub.persistence.repository.UserAccountRepository;
import com.coworkhub.presentacion.dto.MiembroDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class MiembroServiceImpl implements MiembroService {

    private final MemberRepository miembros;
    private final MembershipPlanRepository planes;
    private final UserAccountRepository cuentas;
    private final BookingRepository reservas;
    private final PasswordEncoder passwordEncoder;
    private final ContadorHoras contadorHoras;

    public MiembroServiceImpl(MemberRepository miembros, MembershipPlanRepository planes,
                              UserAccountRepository cuentas, BookingRepository reservas,
                              PasswordEncoder passwordEncoder, ContadorHoras contadorHoras) {
        this.miembros = miembros;
        this.planes = planes;
        this.cuentas = cuentas;
        this.reservas = reservas;
        this.passwordEncoder = passwordEncoder;
        this.contadorHoras = contadorHoras;
    }

    @Override
    public List<MiembroDto.Response> listar(String nombre, MemberStatus estado) {
        boolean conNombre = nombre != null && !nombre.isBlank();
        List<Member> lista;
        if (conNombre && estado != null) {
            lista = miembros.findByFullNameContainingIgnoreCaseAndStatusOrderByIdAsc(nombre.trim(), estado);
        } else if (conNombre) {
            lista = miembros.findByFullNameContainingIgnoreCaseOrderByIdAsc(nombre.trim());
        } else if (estado != null) {
            lista = miembros.findByStatusOrderByIdAsc(estado);
        } else {
            lista = miembros.findAllByOrderByIdAsc();
        }
        return lista.stream().map(MiembroDto.Response::de).toList();
    }

    @Override
    public MiembroDto.Response obtener(Long id) {
        return MiembroDto.Response.de(buscar(id));
    }

    /** Registra el miembro y su cuenta (rol MEMBER) en una sola transaccion: si algo falla, no se guarda ninguno. */
    @Override
    @Transactional
    public MiembroDto.Response crear(MiembroDto.CrearRequest r) {
        String documento = r.documento().trim();
        String email = normalizarEmail(r.email());
        String usuario = r.usuario().trim();
        if (miembros.existsByDocumentNumber(documento)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un miembro con el documento " + documento);
        }
        if (miembros.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un miembro con el email " + email);
        }
        if (cuentas.existsByUsernameIgnoreCase(usuario)) {
            throw new ReglaNegocioException("RN-11", "El usuario '" + usuario + "' ya existe");
        }
        MembershipPlan plan = buscarPlan(r.planId());

        UserAccount cuenta = new UserAccount();
        cuenta.setUsername(usuario);
        cuenta.setPassword(passwordEncoder.encode(r.contrasena()));
        cuenta.setRole(Role.MEMBER);

        Member miembro = new Member();
        miembro.setDocumentNumber(documento);
        miembro.setFullName(r.nombreCompleto().trim());
        miembro.setEmail(email);
        miembro.setStatus(MemberStatus.ACTIVE);
        miembro.setPlan(plan);
        miembro.vincularCuenta(cuenta);
        return MiembroDto.Response.de(miembros.save(miembro));
    }

    @Override
    @Transactional
    public MiembroDto.Response actualizar(Long id, MiembroDto.ActualizarRequest r) {
        Member miembro = buscar(id);
        String email = normalizarEmail(r.email());
        if (miembros.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un miembro con el email " + email);
        }
        miembro.setFullName(r.nombreCompleto().trim());
        miembro.setEmail(email);
        miembro.setPlan(buscarPlan(r.planId()));
        return MiembroDto.Response.de(miembro);
    }

    @Override
    @Transactional
    public MiembroDto.Response suspender(Long id) {
        Member miembro = buscar(id);
        miembro.setStatus(MemberStatus.SUSPENDED);
        return MiembroDto.Response.de(miembro);
    }

    @Override
    @Transactional
    public MiembroDto.Response activar(Long id) {
        Member miembro = buscar(id);
        miembro.setStatus(MemberStatus.ACTIVE);
        return MiembroDto.Response.de(miembro);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Member miembro = buscar(id);
        if (reservas.existsByMemberId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar un miembro que tiene reservas");
        }
        miembros.delete(miembro); // cascade REMOVE: borra tambien su cuenta
    }

    /** RF-10: horas incluidas, usadas, restantes y excedentes del mes. */
    @Override
    public MiembroDto.ConsumoResponse consumo(Long id, YearMonth mes) {
        Member miembro = buscar(id);
        List<Booking> delMes = reservas.findByMemberIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                id, mes.atDay(1).atStartOfDay(), mes.plusMonths(1).atDay(1).atStartOfDay());
        BigDecimal usadas = horas(contadorHoras.minutosConsumidos(delMes));
        BigDecimal incluidas = BigDecimal.valueOf(miembro.getPlan().getIncludedHoursPerMonth()).setScale(2);
        BigDecimal restantes = incluidas.subtract(usadas).max(BigDecimal.ZERO).setScale(2);
        BigDecimal excedentes = usadas.subtract(incluidas).max(BigDecimal.ZERO).setScale(2);
        return new MiembroDto.ConsumoResponse(id, mes.toString(), miembro.getPlan().getName(),
                incluidas, usadas, restantes, excedentes);
    }

    private static BigDecimal horas(long minutos) {
        return BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private Member buscar(Long id) {
        return miembros.findWithDetailsById(id).orElseThrow(() -> new RecursoNoEncontradoException("Miembro", id));
    }

    private MembershipPlan buscarPlan(Long id) {
        return planes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Plan", id));
    }
}
