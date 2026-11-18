
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final MongoTemplate mongoTemplate;
    private final AuthService authService;

    public List<EventResponse> getAll(String search) {
        List<Event> events = search == null || search.isBlank()
                ? eventRepository.findAll()
                : eventRepository.findByNameContainingIgnoreCase(search);

        return events.stream().map(this::toResponse).toList();
    }

    public Event getEntity(String id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Event not found"));
    }

    public EventResponse getById(String id) {
        return toResponse(getEntity(id));
    }

    public EventResponse create(EventRequest request) {

        AppUser user = authService.currentUser();

        Event event = Event.builder()
                .name(request.name())
                .description(request.description())
                .venue(request.venue())
                .eventDate(request.eventDate())
                .totalSeats(request.totalSeats())
                .availableSeats(request.totalSeats())
                .ticketPrice(request.ticketPrice())
                .organizerId(user.getId())
                .build();

        return toResponse(eventRepository.save(event));
    }

    public EventResponse update(String id, EventRequest request) {

        Event event = getEntity(id);
        AppUser user = authService.currentUser();

        if (user.getRole() != Role.ADMIN &&
                !event.getOrganizerId().equals(user.getId())) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "You can only edit your own events");
        }

        // Atomic conditional update protects seat counts.
        int reservedSeats = event.getTotalSeats() - event.getAvailableSeats();

        if (request.totalSeats() < reservedSeats) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "New capacity is below reserved seats");
        }

        Query query = new Query(
                Criteria.where("_id").is(id)
                        .and("totalSeats").is(event.getTotalSeats())
                        .and("availableSeats").is(event.getAvailableSeats()));

        Update update = new Update()
                .set("name", request.name())
                .set("description", request.description())
                .set("venue", request.venue())
                .set("eventDate", request.eventDate())
                .set("totalSeats", request.totalSeats())
                .set("ticketPrice", request.ticketPrice())
                .inc("availableSeats",
                        request.totalSeats() - event.getTotalSeats());

        Event updated = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Event.class);

        if (updated == null) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Event changed. Please retry");
        }

        return toResponse(updated);
    }

    public void delete(String id) {

        getEntity(id);

        if (reservationRepository.existsByEventId(id)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Cannot delete event with reservations");
        }

        eventRepository.deleteById(id);
    }

    public Event takeSeats(String eventId, int seats) {

        Query query = new Query(
                Criteria.where("_id").is(eventId)
                        .and("availableSeats").gte(seats)
                        .and("eventDate").gt(LocalDateTime.now()));

        Update update = new Update().inc("availableSeats", -seats);

        Event event = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Event.class);

        if (event == null) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Event unavailable or insufficient seats");
        }

        return event;
    }

    public void releaseSeats(String eventId, int seats) {

        Query query = new Query(
                Criteria.where("_id").is(eventId));

        Update update = new Update().inc("availableSeats", seats);

        Event updated = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Event.class);

        if (updated == null) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Event not found");
        }
    }

    public EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getVenue(),
                event.getEventDate(),
                event.getTotalSeats(),
                event.getAvailableSeats(),
                event.getTicketPrice(),
                event.getOrganizerId());
    }
}
