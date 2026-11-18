
package com.example.ticket_reservation.controller;

import com.example.ticket_reservation.dto.*;
import com.example.ticket_reservation.service.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService authService;
    private final AdminService adminService;

    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        return authService.listUsers();
    }

    @GetMapping("/stats")
    public StatsResponse getStats() {
        return adminService.getStats();
    }
}
