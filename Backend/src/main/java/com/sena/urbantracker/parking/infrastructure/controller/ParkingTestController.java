package com.sena.urbantracker.parking.infrastructure.controller;

import com.sena.urbantracker.monitoring.application.dto.request.TrackingReqDto;
import com.sena.urbantracker.parking.application.service.ParkingDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/v1/parking/test")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ParkingTestController {

    private final ParkingDetectionService parkingDetectionService;

    /**
     * Endpoint para simular una ubicación GPS y probar la detección de estacionamiento
     */
    @PostMapping("/simulate-location")
    public ResponseEntity<String> simulateLocation(@RequestBody LocationSimulationRequest request) {
        try {
            // Crear objeto TrackingReqDto simulado
            TrackingReqDto trackingReqDto = TrackingReqDto.builder()
                    .vehicleId(request.getVehicleId())
                    .routeId(request.getRouteId())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .timestamp(OffsetDateTime.of(request.getTimestamp(), ZoneOffset.UTC))
                    .build();

            // Procesar la ubicación simulada
            parkingDetectionService.processLocationUpdate(trackingReqDto);

            return ResponseEntity.ok("Ubicación simulada procesada exitosamente");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error procesando ubicación simulada: " + e.getMessage());
        }
    }

    /**
     * Endpoint para simular múltiples ubicaciones de un vehículo estacionado
     */
    @PostMapping("/simulate-parking/{vehicleId}")
    public ResponseEntity<String> simulateParking(
            @PathVariable String vehicleId,
            @RequestParam(defaultValue = "10") int durationMinutes,
            @RequestParam(defaultValue = "4.712388") double baseLat,
            @RequestParam(defaultValue = "-74.072092") double baseLng) {

        try {
            LocalDateTime startTime = LocalDateTime.now().minusMinutes(durationMinutes);

            // Simular ubicaciones cada minuto durante el período de estacionamiento
            for (int i = 0; i <= durationMinutes; i++) {
                LocalDateTime timestamp = startTime.plusMinutes(i);

                // Agregar pequeña variación aleatoria para simular movimiento real
                double latVariation = (Math.random() - 0.5) * 0.0001; // ~10 metros
                double lngVariation = (Math.random() - 0.5) * 0.0001;

                TrackingReqDto trackingReqDto = TrackingReqDto.builder()
                        .vehicleId(vehicleId)
                        .routeId(1L) // Ruta de ejemplo
                        .latitude(BigDecimal.valueOf(baseLat + latVariation))
                        .longitude(BigDecimal.valueOf(baseLng + lngVariation))
                        .timestamp(OffsetDateTime.of(timestamp, ZoneOffset.UTC))
                        .build();

                // Procesar cada ubicación
                parkingDetectionService.processLocationUpdate(trackingReqDto);

                // Pequeña pausa para simular tiempo real
                Thread.sleep(100);
            }

            return ResponseEntity.ok(String.format(
                "Simulación completada: %d ubicaciones procesadas para vehículo %s durante %d minutos",
                durationMinutes + 1, vehicleId, durationMinutes));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error en simulación de estacionamiento: " + e.getMessage());
        }
    }

    /**
     * Endpoint para limpiar el estado de tracking (útil para testing)
     */
    @PostMapping("/reset-tracking")
    public ResponseEntity<String> resetTracking() {
        try {
            // Nota: En implementación real, necesitaríamos un método en el servicio
            // para limpiar el estado de tracking. Por ahora retornamos OK.
            return ResponseEntity.ok("Estado de tracking reseteado (simulado)");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error reseteando tracking: " + e.getMessage());
        }
    }

    // DTOs para las solicitudes
    public static class LocationSimulationRequest {
        private String vehicleId;
        private Long routeId;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private LocalDateTime timestamp;

        // Constructor vacío para Jackson
        public LocationSimulationRequest() {}

        // Getters y setters
        public String getVehicleId() { return vehicleId; }
        public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

        public Long getRouteId() { return routeId; }
        public void setRouteId(Long routeId) { this.routeId = routeId; }

        public BigDecimal getLatitude() { return latitude; }
        public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

        public BigDecimal getLongitude() { return longitude; }
        public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}