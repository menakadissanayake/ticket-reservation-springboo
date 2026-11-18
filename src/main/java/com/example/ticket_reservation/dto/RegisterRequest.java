
package com.example.ticket_reservation.dto;

import com.example.ticket_reservation.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(

        @NotBlank(message = "Username is required") String username,

        @NotBlank(message = "Password is required") String password,

        @NotBlank(message = "Full name is required") String fullName,

        @NotNull(message = "Role is required") Role role

) {
}
