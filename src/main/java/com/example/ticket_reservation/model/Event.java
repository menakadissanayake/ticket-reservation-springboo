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
@Document(collection = "events")
public class Event {

    @Id
    private String id;

    private String name;

    private String description;

    private String venue;

    private LocalDateTime eventDate;

    private int totalSeats;

    private int availableSeats;

    private BigDecimal ticketPrice;

    private String organizerId;
}