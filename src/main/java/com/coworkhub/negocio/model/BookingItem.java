package com.coworkhub.negocio.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Detalle de una reserva: entidad intermedia entre Booking y AdditionalService. */
@Entity
@Table(name = "booking_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_item_booking_service", columnNames = {"booking_id", "service_id"}))
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    /** Precio vigente del servicio en el momento de agregarlo (RN-07): cambiar el precio luego no lo altera. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal appliedUnitPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private AdditionalService service;

    protected BookingItem() {
    }

    public BookingItem(AdditionalService service, int quantity) {
        this.service = service;
        this.quantity = quantity;
        this.appliedUnitPrice = service.getUnitPrice();
    }

    public BigDecimal subtotal() {
        return appliedUnitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() { return id; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getAppliedUnitPrice() { return appliedUnitPrice; }
    public Booking getBooking() { return booking; }
    void setBooking(Booking booking) { this.booking = booking; }
    public AdditionalService getService() { return service; }
}
