
package com.example.ticket_reservation.service;

import com.example.ticket_reservation.dto.StatsResponse;
import com.example.ticket_reservation.model.*;
import com.example.ticket_reservation.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    public StatsResponse getStats() {

        List<Reservation> confirmed = reservationRepository.findAll()
                .stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .toList();

        long ticketsSold = confirmed.stream()
                .mapToLong(Reservation::getSeats)
                .sum();

        BigDecimal revenue = confirmed.stream()
                .map(Reservation::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new StatsResponse(
                userRepository.count(),
                eventRepository.count(),
                reservationRepository.count(),
                ticketsSold,
                revenue);
    }
}
