
package com.example.ticket_reservation.controller;

import com.example.ticket_reservation.dto.*;
import com.example.ticket_reservation.service.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReservationResponse> book(
            @Valid @RequestBody ReservationRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationService.book(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<ReservationResponse> myReservations() {
        return reservationService.myReservations();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public List<ReservationResponse> getAll() {
        return reservationService.getAll();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ReservationResponse cancel(
            @PathVariable String id) {

        return reservationService.cancel(id);
    }
}
