package com.coworkhub.negocio.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_room_time", columnList = "room_id, start_time, end_time"),
        @Index(name = "idx_booking_member_time", columnList = "member_id, start_time")
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private int attendees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal roomCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal servicesCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    /** Cargo cobrado al cancelar (RN-08). Null mientras la reserva no se cancele. */
    @Column(precision = 12, scale = 2)
    private BigDecimal cancellationFee;

    /** Momento de la cancelacion. Decide si las horas se devuelven al plan (RN-08). Atributo adicional al enunciado. */
    private LocalDateTime cancelledAt;

    /** Lado duenio de Room 1 -> N Booking. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /** Lado duenio de Member 1 -> N Booking. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * Composicion: los detalles pertenecen a la reserva, se guardan y se borran con ella (cascade ALL).
     * orphanRemoval: quitar un detalle de la lista lo borra de la base.
     */
    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingItem> items = new ArrayList<>();

    public void agregarItem(BookingItem item) {
        item.setBooking(this);
        items.add(item);
    }

    public boolean quitarItemDeServicio(Long serviceId) {
        return items.removeIf(i -> i.getService().getId().equals(serviceId));
    }

    public Optional<BookingItem> itemDeServicio(Long serviceId) {
        return items.stream().filter(i -> i.getService().getId().equals(serviceId)).findFirst();
    }

    public long duracionMinutos() {
        return Duration.between(startTime, endTime).toMinutes();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public int getAttendees() { return attendees; }
    public void setAttendees(int attendees) { this.attendees = attendees; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public BigDecimal getRoomCost() { return roomCost; }
    public void setRoomCost(BigDecimal roomCost) { this.roomCost = roomCost; }
    public BigDecimal getServicesCost() { return servicesCost; }
    public void setServicesCost(BigDecimal servicesCost) { this.servicesCost = servicesCost; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public BigDecimal getCancellationFee() { return cancellationFee; }
    public void setCancellationFee(BigDecimal cancellationFee) { this.cancellationFee = cancellationFee; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public List<BookingItem> getItems() { return items; }
}
