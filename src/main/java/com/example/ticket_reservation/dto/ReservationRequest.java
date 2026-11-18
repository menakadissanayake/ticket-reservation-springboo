
package com.example.ticket_reservation.dto;

import jakarta.validation.constraints.*;

public record ReservationRequest(

        @NotBlank(message = "Event ID is required") String eventId,

        @Min(value = 1, message = "Minimum 1 seat") @Max(value = 10, message = "Maximum 10 seats") int seats

) {
}
