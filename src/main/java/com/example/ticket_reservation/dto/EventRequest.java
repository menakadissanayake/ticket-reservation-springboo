
package com.example.ticket_reservation.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventRequest(

        @NotBlank(message = "Event name is required") String name,

        @NotBlank(message = "Description is required") String description,

        @NotBlank(message = "Venue is required") String venue,

        @NotNull(message = "Event date is required") @Future(message = "Event date must be in the future") LocalDateTime eventDate,

        @Min(value = 1, message = "At least 1 seat is required") int totalSeats,

        @NotNull(message = "Ticket price is required") @DecimalMin(value = "0.01", message = "Ticket price must be greater than zero") BigDecimal ticketPrice

) {
}
