package com.sena.urbantracker.parking.infrastructure.controller;

import com.sena.urbantracker.parking.application.dto.response.ParkingEventResDto;
import com.sena.urbantracker.parking.application.dto.response.ParkingStatsDto;
import com.sena.urbantracker.parking.application.service.ParkingConfigService;
import com.sena.urbantracker.parking.application.service.ParkingEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/parking")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ParkingController {

    private final ParkingEventService parkingEventService;
    private final ParkingConfigService parkingConfigService;

    /**
     * Obtiene todos los eventos de estacionamiento
     */
    @GetMapping("/events")
    public ResponseEntity<List<ParkingEventResDto>> getAllParkingEvents() {
        List<ParkingEventResDto> events = parkingEventService.findAll();
        return ResponseEntity.ok(events);
    }

    /**
     * Obtiene eventos de estacionamiento activos (vehículos actualmente estacionados)
     */
    @GetMapping("/events/active")
    public ResponseEntity<List<ParkingEventResDto>> getActiveParkingEvents() {
        List<ParkingEventResDto> events = parkingEventService.findActiveEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * Obtiene eventos de estacionamiento por vehículo
     */
    @GetMapping("/events/vehicle/{vehicleId}")
    public ResponseEntity<List<ParkingEventResDto>> getParkingEventsByVehicle(
            @PathVariable String vehicleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<ParkingEventResDto> events;
        if (startDate != null && endDate != null) {
            events = parkingEventService.findByVehicleIdAndDateRange(vehicleId, startDate, endDate);
        } else {
            events = parkingEventService.findByVehicleId(vehicleId);
        }
        return ResponseEntity.ok(events);
    }

    /**
     * Obtiene eventos de estacionamiento por conductor
     */
    @GetMapping("/events/driver/{driverId}")
    public ResponseEntity<List<ParkingEventResDto>> getParkingEventsByDriver(
            @PathVariable Long driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<ParkingEventResDto> events;
        if (startDate != null && endDate != null) {
            events = parkingEventService.findByDriverIdAndDateRange(driverId, startDate, endDate);
        } else {
            events = parkingEventService.findByDriverId(driverId);
        }
        return ResponseEntity.ok(events);
    }

    /**
     * Obtiene eventos de estacionamiento por compañía
     */
    @GetMapping("/events/company/{companyId}")
    public ResponseEntity<List<ParkingEventResDto>> getParkingEventsByCompany(
            @PathVariable Long companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<ParkingEventResDto> events;
        if (startDate != null && endDate != null) {
            events = parkingEventService.findByCompanyIdAndDateRange(companyId, startDate, endDate);
        } else {
            events = parkingEventService.findByCompanyId(companyId);
        }
        return ResponseEntity.ok(events);
    }

    /**
     * Obtiene eventos de estacionamiento recientes (últimas 24 horas)
     */
    @GetMapping("/events/recent")
    public ResponseEntity<List<ParkingEventResDto>> getRecentParkingEvents() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        List<ParkingEventResDto> events = parkingEventService.findRecentEvents(yesterday);
        return ResponseEntity.ok(events);
    }

    /**
     * Obtiene estadísticas de estacionamiento
     */
    @GetMapping("/stats")
    public ResponseEntity<ParkingStatsDto> getParkingStats() {
        ParkingStatsDto stats = parkingEventService.getParkingStats();
        return ResponseEntity.ok(stats);
    }
}