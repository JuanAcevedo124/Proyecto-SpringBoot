/*
 * CoWorkHub - frontend web (HTML + CSS + JavaScript, sin frameworks).
 * Consume la API REST del backend con fetch y el JWT de /auth/login en el encabezado Authorization.
 * Toda regla de negocio y todo permiso se validan en el backend; aqui solo se ocultan opciones que el rol no puede usar.
 */
(() => {
    'use strict';

    /* =============================== Estado y utilidades =============================== */

    const estado = {
        token: sessionStorage.getItem('cwh.token'),
        usuario: JSON.parse(sessionStorage.getItem('cwh.usuario') || 'null'), // {usuario, rol, miembroId}
        cache: {},          // catalogos ya descargados: sedes, salas, equipamientos, servicios, planes
        catalogo: 'sedes',  // pestana activa de Catalogos
    };

    const $ = (sel, raiz = document) => raiz.querySelector(sel);
    const $$ = (sel, raiz = document) => [...raiz.querySelectorAll(sel)];

    const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) =>
        ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

    const moneda = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
    const dinero = (v) => (v === null || v === undefined ? '—' : moneda.format(Number(v)));

    const fechaHora = (iso) => {
        if (!iso) return '—';
        const d = new Date(iso);
        return d.toLocaleDateString('es-CO', { weekday: 'short', day: '2-digit', month: 'short', year: 'numeric' })
            + ' · ' + d.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit', hour12: false });
    };
    const hora = (iso) => (iso ? iso.substring(11, 16) : '');
    const hhmm = (t) => (t ? String(t).substring(0, 5) : '');
    const hoyISO = (dias = 0) => {
        const d = new Date();
        d.setDate(d.getDate() + dias);
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    };
    const mesActual = () => hoyISO().substring(0, 7);

    const etiqueta = (valor, clase = valor) => `<span class="etiqueta e-${esc(clase)}">${esc(valor)}</span>`;

    const esPersonal = () => ['ADMIN', 'RECEPTIONIST'].includes(estado.usuario?.rol);
    const esAdmin = () => estado.usuario?.rol === 'ADMIN';
    const esMiembro = () => estado.usuario?.rol === 'MEMBER';

    /* =============================== Avisos =============================== */

    function avisar(mensaje, tipo = 'ok') {
        const div = document.createElement('div');
        div.className = `aviso ${tipo}`;
        div.innerHTML = mensaje;
        $('#avisos').appendChild(div);
        setTimeout(() => div.remove(), tipo === 'error' ? 7000 : 3500);
    }

    function avisarError(e) {
        const codigo = e.codigo ? `<code>${esc(e.codigo)}</code> ` : '';
        avisar(`${codigo}${esc(e.message || 'Error inesperado')}`, 'error');
    }

    /* =============================== Cliente de la API =============================== */

    class ErrorApi extends Error {
        constructor(status, cuerpo) {
            super(cuerpo?.error || `Error ${status}`);
            this.status = status;
            this.codigo = cuerpo?.codigo;
        }
    }

    async function api(metodo, ruta, cuerpo) {
        const opciones = { method: metodo, headers: {} };
        if (estado.token) opciones.headers.Authorization = `Bearer ${estado.token}`;
        if (cuerpo !== undefined) {
            opciones.headers['Content-Type'] = 'application/json';
            opciones.body = JSON.stringify(cuerpo);
        }
        let resp;
        try {
            resp = await fetch(ruta, opciones);
        } catch {
            throw new ErrorApi(0, { error: 'No se pudo conectar con el servidor', codigo: 'SIN_CONEXION' });
        }
        const texto = await resp.text();
        let datos = null;
        try {
            datos = texto ? JSON.parse(texto) : null;
        } catch {
            datos = { error: `Respuesta inesperada del servidor (${resp.status})` };
        }
        if (!resp.ok) {
            if (resp.status === 401 && estado.token && ruta !== '/auth/login') {
                cerrarSesion();
                avisar('La sesión venció o el token no es válido. Inicie sesión de nuevo.', 'error');
            }
            throw new ErrorApi(resp.status, datos);
        }
        return datos;
    }

    /** Ejecuta una accion y muestra el error del backend si falla. Devuelve true si salio bien. */
    async function intentar(fn) {
        try {
            await fn();
            return true;
        } catch (e) {
            avisarError(e);
            return false;
        }
    }

    const rutaCatalogo = { sedes: 'sedes', salas: 'salas', equipamientos: 'equipamientos', servicios: 'servicios-adicionales', planes: 'planes' };

    async function catalogo(nombre, refrescar = false) {
        if (refrescar || !estado.cache[nombre]) {
            estado.cache[nombre] = await api('GET', `/api/${rutaCatalogo[nombre]}`);
        }
        return estado.cache[nombre];
    }

    const opciones = (lista, valor, texto, seleccionado) => lista.map((x) =>
        `<option value="${esc(x[valor])}" ${String(x[valor]) === String(seleccionado) ? 'selected' : ''}>${esc(texto(x))}</option>`).join('');

    /* =============================== Modal =============================== */

    let alAceptarModal = null;

    function abrirModal({ titulo, cuerpo, textoOk = 'Guardar', alAceptar = null }) {
        $('#modal-titulo').textContent = titulo;
        $('#modal-cuerpo').innerHTML = cuerpo;
        $('#modal-ok').textContent = textoOk;
        $('#modal-ok').classList.toggle('oculto', !alAceptar);
        alAceptarModal = alAceptar;
        if (!$('#modal').open) $('#modal').showModal();
        return $('#modal-cuerpo');
    }

    const cerrarModal = () => $('#modal').close();
    $$('[data-cerrar-modal]').forEach((b) => b.addEventListener('click', cerrarModal));

    $('#modal-form').addEventListener('submit', async (ev) => {
        ev.preventDefault();
        if (!alAceptarModal) return;
        const form = $('#modal-form');
        if (!form.reportValidity()) return;
        $('#modal-ok').disabled = true;
        const ok = await intentar(() => alAceptarModal(new FormData(form), form));
        $('#modal-ok').disabled = false;
        if (ok) cerrarModal();
    });

    /* =============================== Sesion =============================== */

    $('#form-login').addEventListener('submit', async (ev) => {
        ev.preventDefault();
        const f = new FormData(ev.target);
        await intentar(async () => {
            const r = await api('POST', '/auth/login', { usuario: f.get('usuario'), contrasena: f.get('contrasena') });
            estado.token = r.token;
            estado.usuario = { usuario: r.usuario, rol: r.rol, miembroId: r.miembroId };
            sessionStorage.setItem('cwh.token', r.token);
            sessionStorage.setItem('cwh.usuario', JSON.stringify(estado.usuario));
            iniciarApp();
        });
    });

    $$('.chip[data-u]').forEach((b) => b.addEventListener('click', () => {
        $('#form-login [name=usuario]').value = b.dataset.u;
        $('#form-login [name=contrasena]').value = b.dataset.p;
    }));

    $('#btn-salir').addEventListener('click', cerrarSesion);

    function cerrarSesion() {
        estado.token = null;
        estado.usuario = null;
        estado.cache = {};
        sessionStorage.removeItem('cwh.token');
        sessionStorage.removeItem('cwh.usuario');
        $('#vista-app').classList.add('oculto');
        $('#vista-login').classList.remove('oculto');
        $('#form-login [name=contrasena]').value = '';
    }

    /* =============================== Navegacion =============================== */

    const vistas = [
        { id: 'inicio', texto: 'Inicio', cargar: cargarInicio },
        { id: 'disponibilidad', texto: 'Disponibilidad', cargar: prepararDisponibilidad },
        { id: 'reservas', texto: () => (esMiembro() ? 'Mis reservas' : 'Reservas'), cargar: cargarReservas },
        { id: 'miembros', texto: 'Miembros', cargar: cargarMiembros, visible: esPersonal },
        { id: 'catalogos', texto: 'Catálogos', cargar: cargarCatalogo },
    ];

    function iniciarApp() {
        $('#vista-login').classList.add('oculto');
        $('#vista-app').classList.remove('oculto');
        $('#usuario-nombre').textContent = estado.usuario.usuario;
        $('#usuario-rol').textContent = estado.usuario.rol;
        $$('.solo-admin').forEach((el) => el.classList.toggle('oculto', !esAdmin()));
        $$('.solo-personal').forEach((el) => el.classList.toggle('oculto', !esPersonal()));
        $('#menu').innerHTML = vistas.filter((v) => !v.visible || v.visible())
            .map((v) => `<button data-ir="${v.id}">${esc(typeof v.texto === 'function' ? v.texto() : v.texto)}</button>`).join('');
        irA('inicio');
    }

    $('#menu').addEventListener('click', (ev) => {
        const b = ev.target.closest('[data-ir]');
        if (b) irA(b.dataset.ir);
    });

    function irA(id) {
        const vista = vistas.find((v) => v.id === id);
        $$('#menu button').forEach((b) => b.classList.toggle('activo', b.dataset.ir === id));
        $$('.vista').forEach((s) => s.classList.toggle('activa', s.dataset.vista === id));
        intentar(vista.cargar);
    }

    /* =============================== Inicio =============================== */

    async function cargarInicio() {
        $('#saludo').textContent = estado.usuario.usuario;
        const tarjeta = (num, lbl, extra = '') => `<div class="tarjeta"><div class="num">${num}</div><div class="lbl">${esc(lbl)}</div>${extra}</div>`;

        if (esMiembro()) {
            const id = estado.usuario.miembroId;
            const [consumo, reservas] = await Promise.all([
                api('GET', `/api/miembros/${id}/consumo?mes=${mesActual()}`),
                api('GET', `/api/reservas?miembroId=${id}`),
            ]);
            const ahora = new Date();
            const proximas = reservas.filter((r) => ['PENDING', 'CONFIRMED'].includes(r.estado) && new Date(r.inicio) > ahora);
            const pct = consumo.horasIncluidas > 0 ? Math.min(100, (consumo.horasUsadas / consumo.horasIncluidas) * 100) : 100;
            $('#inicio-tarjetas').innerHTML =
                tarjeta(consumo.horasRestantes, 'Horas restantes del plan este mes',
                    `<div class="barra-progreso"><span style="width:${pct}%"></span></div>`)
                + tarjeta(consumo.horasUsadas, `Horas usadas de ${consumo.horasIncluidas} incluidas`)
                + tarjeta(consumo.horasExcedentes, 'Horas excedentes (se cobran)')
                + tarjeta(proximas.length, 'Reservas activas próximas');
            $('#inicio-consumo').innerHTML = `<p class="ayuda">Plan <strong>${esc(consumo.plan)}</strong> · consumo de ${esc(consumo.mes)} (RF-10).</p>`
                + tablaReservas(proximas.slice(0, 5), 'No tiene reservas próximas.');
        } else {
            const peticiones = [catalogo('sedes', true), catalogo('salas', true),
                api('GET', '/api/miembros'), api('GET', '/api/reservas?estado=PENDING'), api('GET', '/api/reservas?estado=CONFIRMED')];
            const [sedes, salas, miembros, pendientes, confirmadas] = await Promise.all(peticiones);
            $('#inicio-tarjetas').innerHTML =
                tarjeta(sedes.length, 'Sedes')
                + tarjeta(salas.filter((s) => s.activa).length, `Salas activas de ${salas.length}`)
                + tarjeta(miembros.length, `Miembros (${miembros.filter((m) => m.estado === 'SUSPENDED').length} suspendidos)`)
                + tarjeta(pendientes.length, 'Reservas pendientes de confirmar')
                + tarjeta(confirmadas.length, 'Reservas confirmadas');
            $('#inicio-consumo').innerHTML = '<h2 style="margin:1rem 0 .6rem">Pendientes de confirmar</h2>'
                + tablaReservas(pendientes, 'No hay reservas pendientes.');
        }
    }

    /* =============================== Disponibilidad =============================== */

    async function prepararDisponibilidad() {
        const [sedes, equipos] = await Promise.all([catalogo('sedes'), catalogo('equipamientos')]);
        const f = $('#form-disp');
        const sedeElegida = f.sedeId.value;
        f.sedeId.innerHTML = opciones(sedes, 'id', (s) => `${s.nombre} (${hhmm(s.horaApertura)}–${hhmm(s.horaCierre)})`, sedeElegida);
        if (!f.fecha.value) f.fecha.value = hoyISO(1);
        if (!$('#disp-equipos').children.length) {
            $('#disp-equipos').innerHTML = equipos.map((e) =>
                `<label><input type="checkbox" name="equipamientoIds" value="${e.id}"> ${esc(e.nombre)}</label>`).join('');
        }
    }

    $('#form-disp').addEventListener('submit', (ev) => {
        ev.preventDefault();
        intentar(async () => {
            const f = new FormData(ev.target);
            const q = new URLSearchParams({ sedeId: f.get('sedeId'), fecha: f.get('fecha'), horaInicio: f.get('horaInicio'), horaFin: f.get('horaFin') });
            if (f.get('capacidadMinima')) q.set('capacidadMinima', f.get('capacidadMinima'));
            f.getAll('equipamientoIds').forEach((id) => q.append('equipamientoIds', id));
            const salas = await api('GET', `/api/disponibilidad?${q}`);
            const sel = { fecha: f.get('fecha'), horaInicio: f.get('horaInicio'), horaFin: f.get('horaFin') };
            $('#disp-resultado').innerHTML = !salas.length
                ? '<div class="panel vacio">No hay salas libres con esos criterios.</div>'
                : `<div class="tabla-envoltura"><table>
                    <thead><tr><th>Sala</th><th>Tipo</th><th class="num">Capacidad</th><th class="num">Tarifa/h</th><th>Equipamiento</th><th></th></tr></thead>
                    <tbody>${salas.map((s) => `<tr>
                        <td><strong>${esc(s.nombre)}</strong><br><small>${esc(s.sedeNombre)}</small></td>
                        <td>${esc(s.tipo)}</td><td class="num">${s.capacidad}</td><td class="num">${dinero(s.tarifaHora)}</td>
                        <td>${esc(s.equipamiento.map((e) => e.nombre).join(', ') || '—')}</td>
                        <td class="acciones"><button class="btn btn-primario btn-chico" data-reservar='${esc(JSON.stringify({ ...sel, sedeId: s.sedeId, salaId: s.id }))}'>Reservar</button></td>
                    </tr>`).join('')}</tbody></table></div>`;
        });
    });

    $('#disp-resultado').addEventListener('click', (ev) => {
        const b = ev.target.closest('[data-reservar]');
        if (b) intentar(() => modalNuevaReserva(JSON.parse(b.dataset.reservar)));
    });

    /* =============================== Reservas =============================== */

    function tablaReservas(lista, textoVacio = 'No hay reservas para mostrar.') {
        if (!lista.length) return `<div class="panel vacio">${esc(textoVacio)}</div>`;
        return `<div class="tabla-envoltura"><table>
            <thead><tr><th>#</th><th>Fecha</th><th>Sala</th>${esMiembro() ? '' : '<th>Miembro</th>'}<th>Estado</th>
                <th class="num">Total</th><th class="num">Cargo cancel.</th><th></th></tr></thead>
            <tbody>${lista.map((r) => `<tr>
                <td>${r.id}</td>
                <td>${fechaHora(r.inicio)}–${hora(r.fin)}</td>
                <td>${esc(r.sala)}<br><small>${esc(r.sede)}</small></td>
                ${esMiembro() ? '' : `<td>${esc(r.miembro)}</td>`}
                <td>${etiqueta(r.estado)}</td>
                <td class="num">${dinero(r.costoTotal)}</td>
                <td class="num">${r.cargoCancelacion === null ? '—' : dinero(r.cargoCancelacion)}</td>
                <td class="acciones">${accionesReserva(r)}</td>
            </tr>`).join('')}</tbody></table></div>`;
    }

    function accionesReserva(r) {
        const b = (accion, texto, clase = '') => `<button class="btn btn-chico ${clase}" data-reserva="${r.id}" data-accion-reserva="${accion}">${texto}</button>`;
        let html = b('ver', 'Ver');
        if (esPersonal() && r.estado === 'PENDING') html += b('confirmar', 'Confirmar', 'btn-primario');
        if (esPersonal() && r.estado === 'CONFIRMED') html += b('completar', 'Completar');
        if (['PENDING', 'CONFIRMED'].includes(r.estado)) html += b('cancelar', 'Cancelar', 'btn-peligro');
        return html;
    }

    async function cargarReservas() {
        const f = $('#form-filtro-reservas');
        f.classList.toggle('oculto', esMiembro());
        if (esPersonal()) {
            const [miembros, salas] = await Promise.all([api('GET', '/api/miembros'), catalogo('salas')]);
            f.miembroId.innerHTML = opciones(miembros, 'id', (m) => `${m.id} · ${m.nombreCompleto}`, f.miembroId.value);
            f.salaId.innerHTML = opciones(salas, 'id', (s) => `${s.nombre} (${s.sedeNombre})`, f.salaId.value);
            if (!f.fecha.value) f.fecha.value = hoyISO(1);
            mostrarFiltroReservas();
        }
        await listarReservas();
    }

    function mostrarFiltroReservas() {
        const tipo = $('#form-filtro-reservas').tipo.value;
        $$('#form-filtro-reservas [data-filtro]').forEach((el) => el.classList.toggle('oculto', el.dataset.filtro !== tipo));
    }

    $('#form-filtro-reservas').tipo.addEventListener('change', mostrarFiltroReservas);
    $('#form-filtro-reservas').addEventListener('submit', (ev) => {
        ev.preventDefault();
        intentar(listarReservas);
    });

    async function listarReservas() {
        let ruta = '/api/reservas';
        if (esMiembro()) {
            ruta += `?miembroId=${estado.usuario.miembroId}`;
        } else {
            const f = $('#form-filtro-reservas');
            const q = { miembro: { miembroId: f.miembroId.value }, sala: { salaId: f.salaId.value, fecha: f.fecha.value }, estado: { estado: f.estado.value } }[f.tipo.value];
            if (q) ruta += `?${new URLSearchParams(q)}`;
        }
        const lista = await api('GET', ruta);
        $('#reservas-lista').innerHTML = tablaReservas(lista);
    }

    async function refrescarVistaActual() {
        const activa = $('.vista.activa')?.dataset.vista;
        const v = vistas.find((x) => x.id === activa);
        if (v) await v.cargar();
    }

    document.addEventListener('click', (ev) => {
        const b = ev.target.closest('[data-accion-reserva]');
        if (!b) return;
        const id = b.dataset.reserva;
        const accion = b.dataset.accionReserva;
        if (accion === 'ver') return intentar(() => modalDetalleReserva(id));
        if (accion === 'cancelar') return modalCancelar(id);
        intentar(async () => {
            const r = await api('POST', `/api/reservas/${id}/${accion}`);
            avisar(`Reserva #${r.id} ahora está ${r.estado}.`);
            await refrescarVistaActual();
        });
    });

    function modalCancelar(id) {
        abrirModal({
            titulo: `Cancelar reserva #${id}`,
            cuerpo: `<p>Regla de cancelación (RN-08):</p>
                <ul><li>24 h o más de anticipación: <strong>sin cargo</strong> y se devuelven las horas.</li>
                <li>Entre 24 h y 2 h: se cobra el <strong>50 %</strong> del costo de la sala.</li>
                <li>Menos de 2 h, o ya empezó: <strong>no se puede cancelar</strong>.</li></ul>`,
            textoOk: 'Cancelar reserva',
            alAceptar: async () => {
                const r = await api('POST', `/api/reservas/${id}/cancelar`);
                avisar(`Reserva #${r.id} cancelada. Cargo: <strong>${dinero(r.cargoCancelacion)}</strong>`);
                await refrescarVistaActual();
            },
        });
    }

    async function modalDetalleReserva(id) {
        const r = await api('GET', `/api/reservas/${id}`);
        const editable = r.estado === 'PENDING';
        const servicios = editable ? await catalogo('servicios') : [];
        const cuerpo = abrirModal({
            titulo: `Reserva #${r.id}`,
            cuerpo: `<div class="detalle"><dl>
                    <dt>Estado</dt><dd>${etiqueta(r.estado)}</dd>
                    <dt>Miembro</dt><dd>${esc(r.miembro)} (#${r.miembroId})</dd>
                    <dt>Sala</dt><dd>${esc(r.sala)} · ${esc(r.sede)}</dd>
                    <dt>Horario</dt><dd>${fechaHora(r.inicio)}–${hora(r.fin)}</dd>
                    <dt>Asistentes</dt><dd>${r.asistentes}</dd>
                    <dt>Costo sala</dt><dd>${dinero(r.costoSala)}</dd>
                    <dt>Servicios</dt><dd>${dinero(r.costoServicios)}</dd>
                    <dt>Total</dt><dd><strong>${dinero(r.costoTotal)}</strong></dd>
                    ${r.cargoCancelacion !== null ? `<dt>Cargo cancelación</dt><dd>${dinero(r.cargoCancelacion)}</dd>` : ''}
                </dl></div>
                <h2>Servicios adicionales</h2>
                ${r.servicios.length ? `<div class="tabla-envoltura"><table>
                    <thead><tr><th>Servicio</th><th class="num">Cant.</th><th class="num">Precio aplicado</th><th class="num">Subtotal</th>${editable ? '<th></th>' : ''}</tr></thead>
                    <tbody>${r.servicios.map((s) => `<tr><td>${esc(s.servicio)}</td><td class="num">${s.cantidad}</td>
                        <td class="num">${dinero(s.precioUnitarioAplicado)}</td><td class="num">${dinero(s.subtotal)}</td>
                        ${editable ? `<td class="acciones"><button type="button" class="btn btn-chico btn-peligro" data-quitar="${s.servicioId}">Quitar</button></td>` : ''}</tr>`).join('')}
                    </tbody></table></div>` : '<p class="ayuda">Sin servicios.</p>'}
                ${editable ? `<div class="fila">
                    <label>Agregar servicio <select name="servicioId">${opciones(servicios, 'id', (s) => `${s.nombre} · ${dinero(s.precioUnitario)}`)}</select></label>
                    <label>Cantidad <input name="cantidad" type="number" min="1" value="1"></label>
                </div>` : '<p class="ayuda">Los servicios solo se cambian mientras la reserva está PENDING (RN-10).</p>'}`,
            textoOk: 'Agregar servicio',
            alAceptar: editable ? async (f) => {
                await api('POST', `/api/reservas/${id}/servicios`, { servicioId: Number(f.get('servicioId')), cantidad: Number(f.get('cantidad')) });
                avisar('Servicio agregado y costo recalculado.');
                await refrescarVistaActual();
                setTimeout(() => intentar(() => modalDetalleReserva(id)), 0);
            } : null,
        });
        $$('[data-quitar]', cuerpo).forEach((b) => b.addEventListener('click', () => intentar(async () => {
            await api('DELETE', `/api/reservas/${id}/servicios/${b.dataset.quitar}`);
            avisar('Servicio quitado y costo recalculado.');
            cerrarModal();
            await refrescarVistaActual();
            await modalDetalleReserva(id);
        })));
    }

    $$('[data-accion="nueva-reserva"]').forEach((b) => b.addEventListener('click', () => intentar(() => modalNuevaReserva({}))));

    async function modalNuevaReserva(pre) {
        const [sedes, salas, servicios] = await Promise.all([catalogo('sedes'), catalogo('salas'), catalogo('servicios')]);
        const miembros = esPersonal() ? (await api('GET', '/api/miembros')) : [];
        const sedeId = pre.sedeId ?? sedes[0]?.id;
        const cuerpo = abrirModal({
            titulo: 'Nueva reserva',
            cuerpo: `${esPersonal()
                ? `<label>Miembro <select name="miembroId" required>${opciones(miembros, 'id', (m) => `${m.nombreCompleto} · ${m.planNombre}${m.estado === 'SUSPENDED' ? ' (SUSPENDIDO)' : ''}`)}</select></label>`
                : `<p class="ayuda">La reserva queda a su nombre.</p>`}
                <div class="fila">
                    <label>Sede <select name="sedeId">${opciones(sedes, 'id', (s) => `${s.nombre} (${hhmm(s.horaApertura)}–${hhmm(s.horaCierre)})`, sedeId)}</select></label>
                    <label>Sala <select name="salaId" required></select></label>
                </div>
                <div class="fila">
                    <label>Fecha <input name="fecha" type="date" required value="${esc(pre.fecha || hoyISO(1))}"></label>
                    <label>Asistentes <input name="asistentes" type="number" min="1" value="1" required></label>
                </div>
                <div class="fila">
                    <label>Desde <input name="horaInicio" type="time" step="1800" required value="${esc(pre.horaInicio || '10:00')}"></label>
                    <label>Hasta <input name="horaFin" type="time" step="1800" required value="${esc(pre.horaFin || '11:00')}"></label>
                </div>
                <h2>Servicios adicionales</h2>
                ${servicios.map((s) => `<div class="servicio-fila"><span>${esc(s.nombre)} · ${dinero(s.precioUnitario)}</span>
                    <input type="number" min="0" value="0" name="serv-${s.id}" aria-label="Cantidad de ${esc(s.nombre)}"></div>`).join('')}
                <p class="ayuda">Duración de 30 min a 8 h en múltiplos de 30 min, dentro del horario de la sede y con 1 h de anticipación.</p>`,
            textoOk: 'Reservar',
            alAceptar: async (f) => {
                const cuerpoReq = {
                    miembroId: esPersonal() ? Number(f.get('miembroId')) : estado.usuario.miembroId,
                    salaId: Number(f.get('salaId')),
                    inicio: `${f.get('fecha')}T${f.get('horaInicio')}:00`,
                    fin: `${f.get('fecha')}T${f.get('horaFin')}:00`,
                    asistentes: Number(f.get('asistentes')),
                    servicios: servicios.map((s) => ({ servicioId: s.id, cantidad: Number(f.get(`serv-${s.id}`) || 0) })).filter((s) => s.cantidad > 0),
                };
                const r = await api('POST', '/api/reservas', cuerpoReq);
                avisar(`Reserva #${r.id} creada (${r.estado}). Sala ${dinero(r.costoSala)} + servicios ${dinero(r.costoServicios)} = <strong>${dinero(r.costoTotal)}</strong>`);
                if ($('.vista.activa')?.dataset.vista === 'reservas') await listarReservas();
            },
        });
        const selSede = $('[name=sedeId]', cuerpo);
        const selSala = $('[name=salaId]', cuerpo);
        const llenarSalas = (salaElegida) => {
            selSala.innerHTML = opciones(salas.filter((s) => String(s.sedeId) === selSede.value), 'id',
                (s) => `${s.nombre} · cap. ${s.capacidad} · ${dinero(s.tarifaHora)}/h${s.activa ? '' : ' (INACTIVA)'}`, salaElegida);
        };
        selSede.addEventListener('change', () => llenarSalas());
        llenarSalas(pre.salaId);
    }

    /* =============================== Miembros =============================== */

    async function cargarMiembros() {
        const f = new FormData($('#form-filtro-miembros'));
        const q = new URLSearchParams();
        if (f.get('nombre')) q.set('nombre', f.get('nombre'));
        if (f.get('estado')) q.set('estado', f.get('estado'));
        const lista = await api('GET', `/api/miembros${q.toString() ? `?${q}` : ''}`);
        $('#miembros-lista').innerHTML = !lista.length ? '<div class="panel vacio">No hay miembros con esos filtros.</div>'
            : `<div class="tabla-envoltura"><table>
                <thead><tr><th>#</th><th>Nombre</th><th>Documento</th><th>Email</th><th>Plan</th><th>Usuario</th><th>Estado</th><th></th></tr></thead>
                <tbody>${lista.map((m) => `<tr>
                    <td>${m.id}</td><td><strong>${esc(m.nombreCompleto)}</strong></td><td>${esc(m.documento)}</td><td>${esc(m.email)}</td>
                    <td>${esc(m.planNombre)}</td><td>${m.usuario ? esc(m.usuario) : '<small>sin cuenta</small>'}</td><td>${etiqueta(m.estado)}</td>
                    <td class="acciones">
                        <button class="btn btn-chico" data-miembro="${m.id}" data-accion-miembro="consumo">Consumo</button>
                        <button class="btn btn-chico" data-miembro="${m.id}" data-accion-miembro="editar">Editar</button>
                        ${m.estado === 'ACTIVE'
                            ? `<button class="btn btn-chico" data-miembro="${m.id}" data-accion-miembro="suspender">Suspender</button>`
                            : `<button class="btn btn-chico" data-miembro="${m.id}" data-accion-miembro="activar">Activar</button>`}
                        <button class="btn btn-chico btn-peligro" data-miembro="${m.id}" data-accion-miembro="eliminar">Eliminar</button>
                    </td></tr>`).join('')}</tbody></table></div>`;
    }

    $('#form-filtro-miembros').addEventListener('submit', (ev) => {
        ev.preventDefault();
        intentar(cargarMiembros);
    });

    $('#miembros-lista').addEventListener('click', (ev) => {
        const b = ev.target.closest('[data-accion-miembro]');
        if (!b) return;
        const id = b.dataset.miembro;
        const accion = b.dataset.accionMiembro;
        if (accion === 'editar') return intentar(() => modalMiembro(id));
        if (accion === 'consumo') return intentar(() => modalConsumo(id, mesActual()));
        if (accion === 'eliminar') {
            return abrirModal({
                titulo: `Eliminar miembro #${id}`,
                cuerpo: '<p>Se eliminará el miembro y su cuenta. No se puede eliminar si tiene reservas (RN-12).</p>',
                textoOk: 'Eliminar',
                alAceptar: async () => {
                    await api('DELETE', `/api/miembros/${id}`);
                    avisar('Miembro eliminado.');
                    await cargarMiembros();
                },
            });
        }
        intentar(async () => {
            const m = await api('PATCH', `/api/miembros/${id}/${accion}`);
            avisar(`${esc(m.nombreCompleto)} ahora está ${m.estado}.`);
            await cargarMiembros();
        });
    });

    $$('[data-accion="nuevo-miembro"]').forEach((b) => b.addEventListener('click', () => intentar(() => modalMiembro(null))));

    async function modalMiembro(id) {
        const planes = await catalogo('planes');
        const m = id ? await api('GET', `/api/miembros/${id}`) : {};
        abrirModal({
            titulo: id ? `Editar miembro #${id}` : 'Registrar miembro y su usuario',
            cuerpo: `${id ? '' : '<label>Documento <input name="documento" required maxlength="30"></label>'}
                <label>Nombre completo <input name="nombreCompleto" required maxlength="120" value="${esc(m.nombreCompleto)}"></label>
                <label>Email <input name="email" type="email" required maxlength="120" value="${esc(m.email)}"></label>
                <label>Plan <select name="planId" required>${opciones(planes, 'id', (p) => `${p.nombre} · ${p.horasIncluidasMes} h/mes`, m.planId)}</select></label>
                ${id ? '' : `<div class="fila">
                    <label>Usuario <input name="usuario" required pattern="[a-zA-Z0-9._\\-]{3,50}" autocomplete="off"></label>
                    <label>Contraseña <input name="contrasena" type="password" required minlength="6" maxlength="72" autocomplete="new-password"></label>
                </div>`}`,
            alAceptar: async (f) => {
                const datos = Object.fromEntries(f.entries());
                datos.planId = Number(datos.planId);
                if (id) {
                    await api('PUT', `/api/miembros/${id}`, { nombreCompleto: datos.nombreCompleto, email: datos.email, planId: datos.planId });
                    avisar('Miembro actualizado.');
                } else {
                    const nuevo = await api('POST', '/api/miembros', datos);
                    avisar(`Miembro #${nuevo.id} registrado con el usuario <strong>${esc(nuevo.usuario)}</strong>.`);
                }
                await cargarMiembros();
            },
        });
    }

    async function modalConsumo(id, mes) {
        const c = await api('GET', `/api/miembros/${id}/consumo?mes=${mes}`);
        const cuerpo = abrirModal({
            titulo: `Consumo del miembro #${id}`,
            cuerpo: `<label>Mes <input type="month" name="mes" value="${esc(c.mes)}"></label>
                <div class="detalle"><dl>
                    <dt>Plan</dt><dd>${esc(c.plan)}</dd>
                    <dt>Horas incluidas</dt><dd>${c.horasIncluidas}</dd>
                    <dt>Horas usadas</dt><dd>${c.horasUsadas}</dd>
                    <dt>Horas restantes</dt><dd><strong>${c.horasRestantes}</strong></dd>
                    <dt>Horas excedentes</dt><dd>${c.horasExcedentes}</dd>
                </dl></div>`,
        });
        $('[name=mes]', cuerpo).addEventListener('change', (ev) => intentar(() => modalConsumo(id, ev.target.value)));
    }

    /* =============================== Catalogos =============================== */

    const tipos = ['MEETING_ROOM', 'PRIVATE_OFFICE', 'DESK'];

    const defCatalogos = {
        sedes: {
            titulo: 'Sedes',
            columnas: [['Nombre', (x) => `<strong>${esc(x.nombre)}</strong>`], ['Ciudad', (x) => esc(x.ciudad)],
                ['Dirección', (x) => esc(x.direccion)], ['Horario', (x) => `${hhmm(x.horaApertura)}–${hhmm(x.horaCierre)}`]],
            formulario: (x) => `<label>Nombre <input name="nombre" required maxlength="100" value="${esc(x.nombre)}"></label>
                <div class="fila"><label>Ciudad <input name="ciudad" required value="${esc(x.ciudad)}"></label>
                <label>Dirección <input name="direccion" required value="${esc(x.direccion)}"></label></div>
                <div class="fila"><label>Apertura <input name="horaApertura" type="time" required value="${hhmm(x.horaApertura)}"></label>
                <label>Cierre <input name="horaCierre" type="time" required value="${hhmm(x.horaCierre)}"></label></div>`,
            datos: (f) => ({ nombre: f.get('nombre'), ciudad: f.get('ciudad'), direccion: f.get('direccion'), horaApertura: f.get('horaApertura'), horaCierre: f.get('horaCierre') }),
        },
        salas: {
            titulo: 'Salas',
            columnas: [['Nombre', (x) => `<strong>${esc(x.nombre)}</strong>`], ['Sede', (x) => esc(x.sedeNombre)], ['Tipo', (x) => esc(x.tipo)],
                ['Capacidad', (x) => x.capacidad, 'num'], ['Tarifa/h', (x) => dinero(x.tarifaHora), 'num'],
                ['Equipamiento', (x) => esc(x.equipamiento.map((e) => e.nombre).join(', ') || '—')],
                ['Estado', (x) => (x.activa ? etiqueta('activa') : etiqueta('inactiva'))]],
            preparar: () => Promise.all([catalogo('sedes'), catalogo('equipamientos')]),
            formulario: (x, [sedes, equipos]) => {
                const tiene = new Set((x.equipamiento || []).map((e) => e.id));
                return `<label>Nombre <input name="nombre" required maxlength="100" value="${esc(x.nombre)}"></label>
                <div class="fila"><label>Sede <select name="sedeId" required>${opciones(sedes, 'id', (s) => s.nombre, x.sedeId)}</select></label>
                <label>Tipo <select name="tipo">${tipos.map((t) => `<option ${t === x.tipo ? 'selected' : ''}>${t}</option>`).join('')}</select></label></div>
                <div class="fila"><label>Capacidad <input name="capacidad" type="number" min="1" required value="${esc(x.capacidad ?? 1)}"></label>
                <label>Tarifa por hora <input name="tarifaHora" type="number" min="0" step="100" required value="${esc(x.tarifaHora ?? '')}"></label></div>
                <fieldset class="checks"><legend>Equipamiento</legend><div>${equipos.map((e) =>
                    `<label><input type="checkbox" name="equipamientoIds" value="${e.id}" ${tiene.has(e.id) ? 'checked' : ''}> ${esc(e.nombre)}</label>`).join('')}</div></fieldset>
                <label style="flex-direction:row;align-items:center;gap:.4rem"><input type="checkbox" name="activa" ${x.activa === false ? '' : 'checked'}> Sala activa</label>`;
            },
            datos: (f) => ({ nombre: f.get('nombre'), sedeId: Number(f.get('sedeId')), tipo: f.get('tipo'), capacidad: Number(f.get('capacidad')),
                tarifaHora: Number(f.get('tarifaHora')), activa: f.get('activa') === 'on', equipamientoIds: f.getAll('equipamientoIds').map(Number) }),
        },
        equipamientos: {
            titulo: 'Equipamiento',
            columnas: [['Nombre', (x) => `<strong>${esc(x.nombre)}</strong>`]],
            formulario: (x) => `<label>Nombre <input name="nombre" required maxlength="80" value="${esc(x.nombre)}"></label>`,
            datos: (f) => ({ nombre: f.get('nombre') }),
        },
        servicios: {
            titulo: 'Servicios adicionales',
            columnas: [['Nombre', (x) => `<strong>${esc(x.nombre)}</strong>`], ['Precio unitario', (x) => dinero(x.precioUnitario), 'num']],
            formulario: (x) => `<label>Nombre <input name="nombre" required maxlength="80" value="${esc(x.nombre)}"></label>
                <label>Precio unitario <input name="precioUnitario" type="number" min="0" step="100" required value="${esc(x.precioUnitario ?? '')}"></label>`,
            datos: (f) => ({ nombre: f.get('nombre'), precioUnitario: Number(f.get('precioUnitario')) }),
        },
        planes: {
            titulo: 'Planes de membresía',
            columnas: [['Nombre', (x) => `<strong>${esc(x.nombre)}</strong>`], ['Horas incluidas/mes', (x) => x.horasIncluidasMes, 'num'],
                ['Descuento excedente', (x) => `${x.descuentoExcedente} %`, 'num'], ['Máx. reservas activas', (x) => x.maxReservasActivas, 'num']],
            formulario: (x) => `<label>Nombre <input name="nombre" required maxlength="80" value="${esc(x.nombre)}"></label>
                <div class="fila"><label>Horas incluidas por mes <input name="horasIncluidasMes" type="number" min="0" required value="${esc(x.horasIncluidasMes ?? '')}"></label>
                <label>Descuento excedente (%) <input name="descuentoExcedente" type="number" min="0" max="100" required value="${esc(x.descuentoExcedente ?? 0)}"></label></div>
                <label>Máximo de reservas activas <input name="maxReservasActivas" type="number" min="1" required value="${esc(x.maxReservasActivas ?? 1)}"></label>`,
            datos: (f) => ({ nombre: f.get('nombre'), horasIncluidasMes: Number(f.get('horasIncluidasMes')),
                descuentoExcedente: Number(f.get('descuentoExcedente')), maxReservasActivas: Number(f.get('maxReservasActivas')) }),
        },
    };

    $('#catalogo-pestanas').innerHTML = Object.entries(defCatalogos)
        .map(([k, d]) => `<button data-catalogo="${k}">${esc(d.titulo)}</button>`).join('');

    $('#catalogo-pestanas').addEventListener('click', (ev) => {
        const b = ev.target.closest('[data-catalogo]');
        if (!b) return;
        estado.catalogo = b.dataset.catalogo;
        intentar(cargarCatalogo);
    });

    async function cargarCatalogo() {
        const clave = estado.catalogo;
        const def = defCatalogos[clave];
        $$('#catalogo-pestanas button').forEach((b) => b.classList.toggle('activo', b.dataset.catalogo === clave));
        $('#catalogo-ayuda').textContent = esAdmin() ? 'Solo el ADMIN crea, modifica o elimina catálogos.' : 'Consulta de catálogos (solo lectura para su rol).';
        const lista = await catalogo(clave, true);
        $('#catalogo-lista').innerHTML = !lista.length ? '<div class="panel vacio">Sin registros.</div>'
            : `<div class="tabla-envoltura"><table>
                <thead><tr><th>#</th>${def.columnas.map(([t, , c]) => `<th class="${c || ''}">${esc(t)}</th>`).join('')}${esAdmin() ? '<th></th>' : ''}</tr></thead>
                <tbody>${lista.map((x) => `<tr><td>${x.id}</td>${def.columnas.map(([, fn, c]) => `<td class="${c || ''}">${fn(x)}</td>`).join('')}
                    ${esAdmin() ? `<td class="acciones">
                        <button class="btn btn-chico" data-cat-editar="${x.id}">Editar</button>
                        <button class="btn btn-chico btn-peligro" data-cat-eliminar="${x.id}">Eliminar</button></td>` : ''}
                </tr>`).join('')}</tbody></table></div>`;
    }

    $('#catalogo-lista').addEventListener('click', (ev) => {
        const editar = ev.target.closest('[data-cat-editar]');
        const eliminar = ev.target.closest('[data-cat-eliminar]');
        if (editar) intentar(() => modalCatalogo(Number(editar.dataset.catEditar)));
        if (eliminar) {
            const id = eliminar.dataset.catEliminar;
            const clave = estado.catalogo;
            abrirModal({
                titulo: `Eliminar de ${defCatalogos[clave].titulo} #${id}`,
                cuerpo: '<p>No se elimina nada que esté en uso (RN-12).</p>',
                textoOk: 'Eliminar',
                alAceptar: async () => {
                    await api('DELETE', `/api/${rutaCatalogo[clave]}/${id}`);
                    avisar('Registro eliminado.');
                    await cargarCatalogo();
                },
            });
        }
    });

    $$('[data-accion="nuevo-catalogo"]').forEach((b) => b.addEventListener('click', () => intentar(() => modalCatalogo(null))));

    async function modalCatalogo(id) {
        const clave = estado.catalogo;
        const def = defCatalogos[clave];
        const extra = def.preparar ? await def.preparar() : null;
        const actual = id ? estado.cache[clave].find((x) => x.id === id) : {};
        abrirModal({
            titulo: `${id ? 'Editar' : 'Nuevo'} · ${def.titulo}`,
            cuerpo: def.formulario(actual, extra),
            alAceptar: async (f) => {
                const ruta = `/api/${rutaCatalogo[clave]}`;
                await (id ? api('PUT', `${ruta}/${id}`, def.datos(f)) : api('POST', ruta, def.datos(f)));
                avisar(id ? 'Registro actualizado.' : 'Registro creado.');
                await cargarCatalogo();
            },
        });
    }

    /* =============================== Arranque =============================== */

    if (estado.token && estado.usuario) {
        // Verifica que el token guardado siga siendo valido antes de mostrar la aplicacion.
        api('GET', '/auth/perfil').then(iniciarApp).catch(() => cerrarSesion());
    }
})();
