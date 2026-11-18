package com.example.ticket_reservation.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "reservations")
public class Reservation {

    @Id
    private String id;

    private String reservationCode;

    private String eventId;

    private String customerId;

    private int seats;

    private BigDecimal totalAmount;

    private ReservationStatus status;

    private LocalDateTime createdAt;
}
