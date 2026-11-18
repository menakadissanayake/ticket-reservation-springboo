
package com.example.ticket_reservation.dto;

import com.example.ticket_reservation.model.ReservationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(

        String id,

        String reservationCode,

        String eventId,

        String customerId,

        int seats,

        BigDecimal totalAmount,

        ReservationStatus status,

        LocalDateTime createdAt

) {
}
