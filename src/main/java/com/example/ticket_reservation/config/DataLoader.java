
package com.example.ticket_reservation.config;

import com.example.ticket_reservation.model.*;
import com.example.ticket_reservation.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadData() {

        return args -> {

            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(
                        AppUser.builder()
                                .username("admin")
                                .password(passwordEncoder.encode("admin123"))
                                .fullName("System Administrator")
                                .role(Role.ADMIN)
                                .build());
            }

            if (!userRepository.existsByUsername("organizer")) {
                userRepository.save(
                        AppUser.builder()
                                .username("organizer")
                                .password(passwordEncoder.encode("organizer123"))
                                .fullName("Event Organizer")
                                .role(Role.ORGANIZER)
                                .build());
            }

            if (!userRepository.existsByUsername("customer")) {
                userRepository.save(
                        AppUser.builder()
                                .username("customer")
                                .password(passwordEncoder.encode("customer123"))
                                .fullName("Sample Customer")
                                .role(Role.CUSTOMER)
                                .build());
            }

            if (eventRepository.count() == 0) {

                AppUser organizer = userRepository
                        .findByUsername("organizer")
                        .orElseThrow();

                Event event1 = Event.builder()
                        .name("Music Concert 2030")
                        .description("Live music event")
                        .venue("Colombo")
                        .eventDate(LocalDateTime.of(
                                2030, 12, 31, 18, 0))
                        .totalSeats(100)
                        .availableSeats(100)
                        .ticketPrice(new BigDecimal("1500.00"))
                        .organizerId(organizer.getId())
                        .build();

                Event event2 = Event.builder()
                        .name("Technology Conference 2030")
                        .description("Technology and innovation")
                        .venue("Kandy")
                        .eventDate(LocalDateTime.of(
                                2030, 11, 20, 9, 0))
                        .totalSeats(50)
                        .availableSeats(50)
                        .ticketPrice(new BigDecimal("2500.00"))
                        .organizerId(organizer.getId())
                        .build();

                eventRepository.save(event1);
                eventRepository.save(event2);
            }

            System.out.println(
                    "Ticket Reservation sample data initialized.");
        };
    }
}
