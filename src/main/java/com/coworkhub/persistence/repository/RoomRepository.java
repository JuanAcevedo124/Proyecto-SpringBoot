package com.coworkhub.persistence.repository;

import com.coworkhub.negocio.model.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    /** El EntityGraph trae sede y equipamiento en la misma consulta: evita el problema N+1 en los listados. */
    @EntityGraph(attributePaths = {"location", "equipment"})
    List<Room> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"location", "equipment"})
    List<Room> findByLocationIdOrderByIdAsc(Long locationId);

    @EntityGraph(attributePaths = {"location", "equipment"})
    Optional<Room> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"location", "equipment"})
    List<Room> findByLocationIdAndActiveTrueAndCapacityGreaterThanEqualOrderByIdAsc(Long locationId, int capacity);

    /**
     * SELECT ... FOR UPDATE sobre la sala: serializa la creacion de reservas de una misma sala
     * para que dos solicitudes simultaneas no puedan crear reservas que se crucen (RN-01).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r join fetch r.location where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    boolean existsByLocationId(Long locationId);

    boolean existsByLocationIdAndNameIgnoreCase(Long locationId, String name);

    boolean existsByLocationIdAndNameIgnoreCaseAndIdNot(Long locationId, String name, Long id);

    boolean existsByEquipmentId(Long equipmentId);
}
