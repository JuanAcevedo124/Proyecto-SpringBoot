package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.coworkhub.negocio.model.Booking;
import com.coworkhub.negocio.model.BookingItem;
import com.coworkhub.negocio.model.BookingStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/** DTO de reservas. */
public final class ReservaDto {

    private ReservaDto() {
    }

    @Schema(name = "ReservaCrearRequest")
    public record CrearRequest(
            @Schema(example = "1") @NotNull(message = "El miembro es obligatorio") Long miembroId,
            @Schema(example = "5") @NotNull(message = "La sala es obligatoria") Long salaId,
            @Schema(type = "string", example = "2030-06-03T09:00:00") @NotNull(message = "La fecha y hora de inicio es obligatoria") LocalDateTime inicio,
            @Schema(type = "string", example = "2030-06-03T11:00:00") @NotNull(message = "La fecha y hora de fin es obligatoria") LocalDateTime fin,
            @Schema(example = "4") @NotNull(message = "El numero de asistentes es obligatorio") @Min(value = 1, message = "Debe haber al menos 1 asistente") Integer asistentes,
            List<@Valid ServicioRequest> servicios) {

        public List<ServicioRequest> serviciosOVacio() {
            return servicios == null ? List.of() : servicios;
        }
    }

    @Schema(name = "ReservaServicioRequest")
    public record ServicioRequest(
            @Schema(example = "1") @NotNull(message = "El servicio es obligatorio") Long servicioId,
            @Schema(example = "2") @NotNull(message = "La cantidad es obligatoria") @Min(value = 1, message = "La cantidad minima es 1") Integer cantidad) {
    }

    @Schema(name = "ReservaDetalleResponse")
    public record DetalleResponse(Long servicioId, String servicio, int cantidad, BigDecimal precioUnitarioAplicado,
                                  BigDecimal subtotal) {

        public static DetalleResponse de(BookingItem i) {
            return new DetalleResponse(i.getService().getId(), i.getService().getName(), i.getQuantity(),
                    i.getAppliedUnitPrice(), i.subtotal());
        }
    }

    @Schema(name = "ReservaResponse")
    public record Response(Long id, Long miembroId, String miembro, Long salaId, String sala, String sede,
                           LocalDateTime inicio, LocalDateTime fin, int asistentes, BookingStatus estado,
                           BigDecimal costoSala, BigDecimal costoServicios, BigDecimal costoTotal,
                           BigDecimal cargoCancelacion, List<DetalleResponse> servicios) {

        public static Response de(Booking b) {
            List<DetalleResponse> detalles = b.getItems().stream()
                    .sorted(Comparator.comparing(i -> i.getService().getId()))
                    .map(DetalleResponse::de)
                    .toList();
            return new Response(b.getId(), b.getMember().getId(), b.getMember().getFullName(),
                    b.getRoom().getId(), b.getRoom().getName(), b.getRoom().getLocation().getName(),
                    b.getStartTime(), b.getEndTime(), b.getAttendees(), b.getStatus(),
                    b.getRoomCost(), b.getServicesCost(), b.getTotalCost(), b.getCancellationFee(), detalles);
        }
    }
}
