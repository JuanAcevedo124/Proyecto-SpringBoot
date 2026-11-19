-- =====================================================================================
-- CoWorkHub - datos de prueba. Hibernate lo ejecuta al arrancar con ddl-auto=create.
-- ADVERTENCIA EDUCATIVA: usuarios y contrasenas de prueba (admin123, recep123, ana123,
-- luis123), solo para desarrollo. Las personas y correos son ficticios.
-- Las fechas se calculan a partir de hoy para que las reservas futuras sigan siendo futuras.
-- =====================================================================================

-- Sedes (la Sede Norte abre todo el dia para poder probar cancelaciones a cualquier hora)
INSERT INTO locations (id, name, city, address, opening_time, closing_time) VALUES (1, 'Sede Centro', 'Bogota', 'Calle 10 # 5-20', '07:00:00', '21:00:00');
INSERT INTO locations (id, name, city, address, opening_time, closing_time) VALUES (2, 'Sede Norte 24h', 'Medellin', 'Carrera 43A # 1-50', '00:00:00', '23:59:00');

-- Equipamiento
INSERT INTO equipment (id, name) VALUES (1, 'Proyector');
INSERT INTO equipment (id, name) VALUES (2, 'Pizarra');
INSERT INTO equipment (id, name) VALUES (3, 'Videoconferencia');
INSERT INTO equipment (id, name) VALUES (4, 'Televisor 55 pulgadas');

-- Salas (la 4 esta inactiva)
INSERT INTO rooms (id, name, type, capacity, hourly_rate, active, location_id) VALUES (1, 'Sala Andes', 'MEETING_ROOM', 8, 50000.00, true, 1);
INSERT INTO rooms (id, name, type, capacity, hourly_rate, active, location_id) VALUES (2, 'Oficina Caribe', 'PRIVATE_OFFICE', 4, 80000.00, true, 1);
INSERT INTO rooms (id, name, type, capacity, hourly_rate, active, location_id) VALUES (3, 'Escritorio Pacifico', 'DESK', 1, 15000.00, true, 1);
INSERT INTO rooms (id, name, type, capacity, hourly_rate, active, location_id) VALUES (4, 'Sala Orinoquia', 'MEETING_ROOM', 12, 70000.00, false, 1);
INSERT INTO rooms (id, name, type, capacity, hourly_rate, active, location_id) VALUES (5, 'Sala Nevado', 'MEETING_ROOM', 10, 60000.00, true, 2);
INSERT INTO rooms (id, name, type, capacity, hourly_rate, active, location_id) VALUES (6, 'Oficina Amazonas', 'PRIVATE_OFFICE', 6, 90000.00, true, 2);
INSERT INTO rooms (id, name, type, capacity, hourly_rate, active, location_id) VALUES (7, 'Escritorio Guajira', 'DESK', 1, 20000.00, true, 2);

-- Equipamiento de cada sala
INSERT INTO room_equipment (room_id, equipment_id) VALUES (1, 1);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (1, 2);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (1, 3);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (2, 2);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (2, 4);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (4, 1);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (5, 1);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (5, 3);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (6, 2);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (6, 3);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (6, 4);

-- Servicios adicionales
INSERT INTO additional_services (id, name, unit_price) VALUES (1, 'Cafe ilimitado', 8000.00);
INSERT INTO additional_services (id, name, unit_price) VALUES (2, 'Almuerzo ejecutivo', 25000.00);
INSERT INTO additional_services (id, name, unit_price) VALUES (3, 'Paquete de 50 impresiones', 12000.00);
INSERT INTO additional_services (id, name, unit_price) VALUES (4, 'Parqueadero', 10000.00);

-- Planes de membresia
INSERT INTO membership_plans (id, name, included_hours_per_month, overage_discount, max_active_bookings) VALUES (1, 'Basico', 5, 0, 2);
INSERT INTO membership_plans (id, name, included_hours_per_month, overage_discount, max_active_bookings) VALUES (2, 'Profesional', 20, 10, 5);
INSERT INTO membership_plans (id, name, included_hours_per_month, overage_discount, max_active_bookings) VALUES (3, 'Premium', 40, 20, 30);

-- Cuentas de usuario (BCrypt). admin/admin123, recepcion/recep123, ana/ana123, luis/luis123
INSERT INTO user_accounts (id, username, password, role) VALUES (1, 'admin', '$2a$10$Wk20wXWT4bsRxdhY2UCIZefYO3qnh8GtngZVOhrBjQBREjFlak9SS', 'ADMIN');
INSERT INTO user_accounts (id, username, password, role) VALUES (2, 'recepcion', '$2a$10$OBriePvL3mq6FDRpAzpt3eZDD.RTlBakNPZ6i4H2qwLBvNdDe8O2m', 'RECEPTIONIST');
INSERT INTO user_accounts (id, username, password, role) VALUES (3, 'ana', '$2a$10$r2q1Az0dCV52O.p0Kb0Fd.36r5.CYNkcqSb5sQVRQUif//.xMqO2e', 'MEMBER');
INSERT INTO user_accounts (id, username, password, role) VALUES (4, 'luis', '$2a$10$ezktWjuyO1enxs3bEEAKD.WxR4Lj1lz7JDlG8olq0r.VoDTsBmqDW', 'MEMBER');

-- Miembros (Carla, Diego y Elena no tienen cuenta; Elena esta suspendida)
INSERT INTO members (id, document_number, full_name, email, status, plan_id, user_account_id) VALUES (1, '1001001001', 'Ana Gomez', 'ana.gomez@example.com', 'ACTIVE', 3, 3);
INSERT INTO members (id, document_number, full_name, email, status, plan_id, user_account_id) VALUES (2, '1001001002', 'Luis Perez', 'luis.perez@example.com', 'ACTIVE', 2, 4);
INSERT INTO members (id, document_number, full_name, email, status, plan_id, user_account_id) VALUES (3, '1001001003', 'Carla Ruiz', 'carla.ruiz@example.com', 'ACTIVE', 1, NULL);
INSERT INTO members (id, document_number, full_name, email, status, plan_id, user_account_id) VALUES (4, '1001001004', 'Diego Torres', 'diego.torres@example.com', 'ACTIVE', 2, NULL);
INSERT INTO members (id, document_number, full_name, email, status, plan_id, user_account_id) VALUES (5, '1001001005', 'Elena Martinez', 'elena.martinez@example.com', 'SUSPENDED', 1, NULL);

-- Reservas (15) en distintos estados. Las salas 6 y 7 quedan libres para las pruebas de aceptacion.
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (1, TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '09:00:00'), TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '11:00:00'), 5, 'COMPLETED', 0.00, 16000.00, 16000.00, NULL, NULL, 1, 1);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (2, TIMESTAMP(CURDATE() - INTERVAL 5 DAY, '14:00:00'), TIMESTAMP(CURDATE() - INTERVAL 5 DAY, '16:00:00'), 2, 'COMPLETED', 0.00, 0.00, 0.00, NULL, NULL, 2, 1);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (3, TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '10:00:00'), TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '12:00:00'), 6, 'COMPLETED', 0.00, 50000.00, 50000.00, NULL, NULL, 1, 2);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (4, TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '08:00:00'), TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '12:00:00'), 1, 'COMPLETED', 0.00, 0.00, 0.00, NULL, NULL, 3, 3);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (5, TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '15:00:00'), TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '17:00:00'), 7, 'COMPLETED', 0.00, 0.00, 0.00, NULL, NULL, 5, 4);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (6, TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '09:00:00'), TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '10:00:00'), 4, 'CONFIRMED', 0.00, 24000.00, 24000.00, NULL, NULL, 1, 1);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (7, TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '10:00:00'), TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '12:00:00'), 3, 'PENDING', 0.00, 12000.00, 12000.00, NULL, NULL, 2, 2);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (8, TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '08:00:00'), TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '10:00:00'), 1, 'PENDING', 15000.00, 0.00, 15000.00, NULL, NULL, 3, 3);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (9, TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '13:00:00'), TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '15:00:00'), 8, 'CONFIRMED', 0.00, 0.00, 0.00, NULL, NULL, 5, 4);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (10, TIMESTAMP(CURDATE() + INTERVAL 3 DAY, '14:00:00'), TIMESTAMP(CURDATE() + INTERVAL 3 DAY, '16:00:00'), 6, 'PENDING', 0.00, 132000.00, 132000.00, NULL, NULL, 1, 1);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (11, TIMESTAMP(CURDATE() + INTERVAL 4 DAY, '09:00:00'), TIMESTAMP(CURDATE() + INTERVAL 4 DAY, '11:00:00'), 4, 'CANCELLED', 0.00, 0.00, 0.00, 0.00, TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '10:00:00'), 1, 2);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (12, TIMESTAMP(CURDATE() - INTERVAL 6 DAY, '09:00:00'), TIMESTAMP(CURDATE() - INTERVAL 6 DAY, '10:00:00'), 1, 'CANCELLED', 15000.00, 0.00, 15000.00, 7500.00, TIMESTAMP(CURDATE() - INTERVAL 6 DAY, '06:00:00'), 3, 3);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (13, TIMESTAMP(CURDATE() + INTERVAL 7 DAY, '09:00:00'), TIMESTAMP(CURDATE() + INTERVAL 7 DAY, '13:00:00'), 9, 'CONFIRMED', 0.00, 10000.00, 10000.00, NULL, NULL, 5, 1);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (14, TIMESTAMP(CURDATE() + INTERVAL 8 DAY, '15:00:00'), TIMESTAMP(CURDATE() + INTERVAL 8 DAY, '17:00:00'), 2, 'PENDING', 0.00, 0.00, 0.00, NULL, NULL, 2, 2);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, cancelled_at, room_id, member_id) VALUES (15, TIMESTAMP(CURDATE() + INTERVAL 9 DAY, '10:00:00'), TIMESTAMP(CURDATE() + INTERVAL 9 DAY, '11:00:00'), 1, 'PENDING', 15000.00, 0.00, 15000.00, NULL, NULL, 3, 3);

-- Detalles de reserva (servicios adicionales con el precio aplicado al reservar)
INSERT INTO booking_items (id, quantity, applied_unit_price, booking_id, service_id) VALUES (1, 2, 8000.00, 1, 1);
INSERT INTO booking_items (id, quantity, applied_unit_price, booking_id, service_id) VALUES (2, 2, 25000.00, 3, 2);
INSERT INTO booking_items (id, quantity, applied_unit_price, booking_id, service_id) VALUES (3, 3, 8000.00, 6, 1);
INSERT INTO booking_items (id, quantity, applied_unit_price, booking_id, service_id) VALUES (4, 1, 12000.00, 7, 3);
INSERT INTO booking_items (id, quantity, applied_unit_price, booking_id, service_id) VALUES (5, 4, 25000.00, 10, 2);
INSERT INTO booking_items (id, quantity, applied_unit_price, booking_id, service_id) VALUES (6, 4, 8000.00, 10, 1);
INSERT INTO booking_items (id, quantity, applied_unit_price, booking_id, service_id) VALUES (7, 1, 10000.00, 13, 4);
