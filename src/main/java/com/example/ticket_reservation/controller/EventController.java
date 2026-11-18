
package com.example.ticket_reservation.controller;

import com.example.ticket_reservation.dto.*;
import com.example.ticket_reservation.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public List<EventResponse> getAll(
            @RequestParam(required = false) String search) {

        return eventService.getAll(search);
    }

    @GetMapping("/{id}")
    public EventResponse getById(@PathVariable String id) {
        return eventService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public ResponseEntity<EventResponse> create(
            @Valid @RequestBody EventRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public EventResponse update(
            @PathVariable String id,
            @Valid @RequestBody EventRequest request) {

        return eventService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
