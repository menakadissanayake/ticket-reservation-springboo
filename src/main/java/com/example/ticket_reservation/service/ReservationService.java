
package com.example.ticket_reservation.service;

import com.example.ticket_reservation.dto.*;
import com.example.ticket_reservation.exception.ApiException;
import com.example.ticket_reservation.model.*;
import com.example.ticket_reservation.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventService eventService;
    private final AuthService authService;
    private final MongoTemplate mongoTemplate;

    public ReservationResponse book(ReservationRequest request) {

        AppUser customer = authService.currentUser();

        Event event = eventService.takeSeats(
                request.eventId(),
                request.seats());

        BigDecimal amount = event.getTicketPrice()
                .multiply(BigDecimal.valueOf(request.seats()));

        String code = "TKT-" +
                UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase();

        Reservation reservation = Reservation.builder()
                .reservationCode(code)
                .eventId(event.getId())
                .customerId(customer.getId())
                .seats(request.seats())
                .totalAmount(amount)
                .status(ReservationStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            return toResponse(
                    reservationRepository.save(reservation));
        } catch (RuntimeException ex) {
            // Best-effort compensation if saving fails.
            eventService.releaseSeats(event.getId(), request.seats());
            throw ex;
        }
    }

    public List<ReservationResponse> myReservations() {

        AppUser customer = authService.currentUser();

        return reservationRepository
                .findByCustomerId(customer.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReservationResponse> getAll() {

        AppUser user = authService.currentUser();

        List<Reservation> reservations;

        if (user.getRole() == Role.ADMIN) {
            reservations = reservationRepository.findAll();
        } else if (user.getRole() == Role.ORGANIZER) {

            List<String> eventIds = eventService.getAll(null).stream()
                    .filter(e -> user.getId()
                            .equals(e.organizerId()))
                    .map(EventResponse::id)
                    .toList();

            reservations = reservationRepository.findAll()
                    .stream()
                    .filter(r -> eventIds.contains(r.getEventId()))
                    .toList();
        } else {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Access denied");
        }

        return reservations.stream()
                .map(this::toResponse)
                .toList();
    }

    public ReservationResponse cancel(String id) {

        AppUser user = authService.currentUser();

        Reservation reservation = reservationRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Reservation not found"));

        if (user.getRole() != Role.ADMIN &&
                !reservation.getCustomerId().equals(user.getId())) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "You can only cancel your own reservation");
        }

        Query query = new Query(
                Criteria.where("_id").is(id)
                        .and("status").is(ReservationStatus.CONFIRMED));

        Update update = new Update()
                .set("status", ReservationStatus.CANCELLED);

        Reservation cancelled = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Reservation.class);

        if (cancelled == null) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Reservation already cancelled");
        }

        eventService.releaseSeats(
                cancelled.getEventId(),
                cancelled.getSeats());

        return toResponse(cancelled);
    }

    public ReservationResponse toResponse(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getReservationCode(),
                r.getEventId(),
                r.getCustomerId(),
                r.getSeats(),
                r.getTotalAmount(),
                r.getStatus(),
                r.getCreatedAt());
    }
}
