package com.coworkhub.persistence.repository;

import com.coworkhub.negocio.model.Booking;
import com.coworkhub.negocio.model.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /* ---------- Listados: un EntityGraph trae sala, sede, miembro y detalles en UNA sola consulta (sin N+1) ---------- */

    @EntityGraph(attributePaths = {"room", "room.location", "member", "items", "items.service"})
    List<Booking> findAllByOrderByStartTimeAsc();

    @EntityGraph(attributePaths = {"room", "room.location", "member", "items", "items.service"})
    List<Booking> findByMemberIdOrderByStartTimeAsc(Long memberId);

    @EntityGraph(attributePaths = {"room", "room.location", "member", "items", "items.service"})
    List<Booking> findByStatusOrderByStartTimeAsc(BookingStatus status);

    @EntityGraph(attributePaths = {"room", "room.location", "member", "items", "items.service"})
    List<Booking> findByRoomIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
            Long roomId, LocalDateTime desde, LocalDateTime hasta);

    @EntityGraph(attributePaths = {"room", "room.location", "member", "member.plan", "items", "items.service"})
    Optional<Booking> findWithDetailsById(Long id);

    /* ---------- Reglas de negocio ---------- */

    /** RN-01: hay cruce si (inicio < finExistente) y (fin > inicioExistente). Los extremos que se tocan no cruzan. */
    @Query("""
            select count(b) > 0 from Booking b
            where b.room.id = :salaId and b.status in :estados
              and b.startTime < :fin and b.endTime > :inicio
            """)
    boolean existeCruce(@Param("salaId") Long salaId,
                        @Param("estados") Collection<BookingStatus> estados,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

    /** RF-03: salas de una sede ocupadas en un rango. */
    @Query("""
            select distinct b.room.id from Booking b
            where b.room.location.id = :sedeId and b.status in :estados
              and b.startTime < :fin and b.endTime > :inicio
            """)
    List<Long> salasOcupadas(@Param("sedeId") Long sedeId,
                             @Param("estados") Collection<BookingStatus> estados,
                             @Param("inicio") LocalDateTime inicio,
                             @Param("fin") LocalDateTime fin);

    /** RN-06: reservas activas futuras del miembro. */
    long countByMemberIdAndStatusInAndStartTimeAfter(Long memberId, Collection<BookingStatus> estados, LocalDateTime ahora);

    /** RN-07 / RF-10: reservas del miembro que empiezan en un mes. */
    List<Booking> findByMemberIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            Long memberId, LocalDateTime desde, LocalDateTime hasta);

    /** Usado por la verificacion de duenio (RN-13) sin cargar la reserva completa. */
    @Query("select b.member.id from Booking b where b.id = :id")
    Optional<Long> findMemberIdByBookingId(@Param("id") Long id);

    boolean existsByRoomId(Long roomId);

    boolean existsByMemberId(Long memberId);
}
