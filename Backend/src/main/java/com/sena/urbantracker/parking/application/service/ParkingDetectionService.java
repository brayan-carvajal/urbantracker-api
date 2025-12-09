package com.sena.urbantracker.parking.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sena.urbantracker.monitoring.application.dto.request.TrackingReqDto;
import com.sena.urbantracker.parking.domain.entity.ParkingConfigDomain;
import com.sena.urbantracker.parking.domain.entity.ParkingEventDomain;
import com.sena.urbantracker.parking.domain.repository.ParkingConfigRepository;
import com.sena.urbantracker.parking.domain.repository.ParkingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingDetectionService {

    private final ParkingConfigRepository parkingConfigRepository;
    private final ParkingEventRepository parkingEventRepository;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // Cache temporal para tracking por vehículo (en memoria, por simplicidad)
    private final Map<String, VehicleTrackingInfo> vehicleTracking = new HashMap<>();

    /**
     * Procesa una ubicación GPS entrante para detectar estacionamiento
     */
    @Transactional
    public void processLocationUpdate(TrackingReqDto trackingReqDto) {
        try {
            String vehicleId = trackingReqDto.getVehicleId();
            Long routeId = trackingReqDto.getRouteId();
            
            // Obtener configuración de la empresa (por ahora usar configuración por defecto)
            Optional<ParkingConfigDomain> configOpt = getApplicableConfig(vehicleId, routeId);
            if (!configOpt.isPresent()) {
                log.debug("No hay configuración de estacionamiento para vehículo: {}", vehicleId);
                return;
            }

            ParkingConfigDomain config = configOpt.get();
            if (!config.getIsActive()) {
                log.debug("Detección de estacionamiento deshabilitada para vehículo: {}", vehicleId);
                return;
            }

            // Obtener o crear tracking info para este vehículo
            VehicleTrackingInfo trackingInfo = getVehicleTrackingInfo(vehicleId);
            
            // Procesar la ubicación
            processLocationForVehicle(trackingReqDto, config, trackingInfo);

        } catch (Exception e) {
            log.error("Error procesando ubicación GPS para estacionamiento: {}", e.getMessage(), e);
        }
    }

    private void processLocationForVehicle(TrackingReqDto trackingReqDto, ParkingConfigDomain config, VehicleTrackingInfo trackingInfo) {
        String vehicleId = trackingReqDto.getVehicleId();
        LocalDateTime currentTime = trackingReqDto.getTimestamp().toLocalDateTime();
        
        // Extraer información adicional del payload si está disponible
        Double currentSpeed = extractSpeed(trackingReqDto);
        BigDecimal currentLat = trackingReqDto.getLatitude();
        BigDecimal currentLng = trackingReqDto.getLongitude();

        // Primera ubicación del vehículo
        if (trackingInfo.getLastLocation() == null) {
            trackingInfo.setLastLocation(new LocationInfo(currentLat, currentLng, currentTime, currentSpeed));
            return;
        }

        LocationInfo lastLocation = trackingInfo.getLastLocation();
        
        // Calcular distancia y velocidad
        double distanceMeters = calculateDistance(
                lastLocation.getLatitude().doubleValue(),
                lastLocation.getLongitude().doubleValue(),
                currentLat.doubleValue(),
                currentLng.doubleValue()
        );

        Duration timeDifference = Duration.between(lastLocation.getTimestamp(), currentTime);
        double speedKmh = calculateSpeed(distanceMeters, timeDifference.toMillis());

        // Actualizar tracking info
        trackingInfo.updateLocation(new LocationInfo(currentLat, currentLng, currentTime, currentSpeed));

        // Detectar si el vehículo está estacionado
        boolean isParked = isVehicleParked(distanceMeters, speedKmh, config);

        if (isParked) {
            trackingInfo.incrementParkedTime(timeDifference);
            
            // Verificar si supera el umbral configurado
            if (trackingInfo.getAccumulatedParkedMinutes() >= config.getMinTimeMinutes()) {
                handleParkingEventDetected(trackingReqDto, config, trackingInfo);
            }
        } else {
            // Vehículo se movió, reiniciar contador
            trackingInfo.resetParkedTime();
            
            // Si había un evento de estacionamiento activo, cerrarlo
            if (trackingInfo.getActiveParkingEventId() != null) {
                handleParkingEventEnded(trackingReqDto, trackingInfo);
            }
        }
    }

    private boolean isVehicleParked(double distanceMeters, double speedKmh, ParkingConfigDomain config) {
        boolean closeDistance = distanceMeters <= config.getMaxDistanceMeters();
        boolean lowSpeed = speedKmh <= config.getMaxSpeedKmh();
        
        return closeDistance && lowSpeed;
    }

    private void handleParkingEventDetected(TrackingReqDto trackingReqDto, ParkingConfigDomain config, VehicleTrackingInfo trackingInfo) {
        String vehicleId = trackingReqDto.getVehicleId();
        Long routeId = trackingReqDto.getRouteId();
        
        // Si ya hay un evento activo, no crear otro
        if (trackingInfo.getActiveParkingEventId() != null) {
            log.debug("Ya existe un evento de estacionamiento activo para vehículo: {}", vehicleId);
            return;
        }

        // Crear nuevo evento de estacionamiento
        ParkingEventDomain parkingEvent = ParkingEventDomain.builder()
                .vehicleId(vehicleId)
                .driverId(null) // Se puede obtener del vehicleId o routeId
                .routeId(routeId)
                .startedAt(trackingReqDto.getTimestamp().toLocalDateTime())
                .isActive(true)
                .finalLocationLat(trackingReqDto.getLatitude())
                .finalLocationLng(trackingReqDto.getLongitude())
                .active(true)
                .build();

        ParkingEventDomain savedEvent = parkingEventRepository.save(parkingEvent);
        trackingInfo.setActiveParkingEventId(savedEvent.getId());

        log.info("🚗🚗 EVENTO DE ESTACIONAMIENTO DETECTADO 🚗🚗");
        log.info("Vehículo: {}", vehicleId);
        log.info("Ruta: {}", routeId);
        log.info("Configuración: {} min, {}m, {} km/h", 
                config.getMinTimeMinutes(), 
                config.getMaxDistanceMeters(), 
                config.getMaxSpeedKmh());
        log.info("ID del evento: {}", savedEvent.getId());
        
        // Aquí se puede enviar notificación via WebSocket al frontend
        sendParkingAlert(savedEvent, "VEHÍCULO_ESTACIONADO", "Vehículo estacionado por tiempo excesivo");
    }

    private void handleParkingEventEnded(TrackingReqDto trackingReqDto, VehicleTrackingInfo trackingInfo) {
        Long eventId = trackingInfo.getActiveParkingEventId();
        if (eventId == null) return;

        Optional<ParkingEventDomain> eventOpt = parkingEventRepository.findById(eventId);
        if (!eventOpt.isPresent()) {
            trackingInfo.setActiveParkingEventId(null);
            return;
        }

        ParkingEventDomain event = eventOpt.get();
        LocalDateTime endTime = trackingReqDto.getTimestamp().toLocalDateTime();
        Duration eventDuration = Duration.between(event.getStartedAt(), endTime);
        int totalMinutes = (int) eventDuration.toMinutes();

        // Actualizar evento
        event.setEndedAt(endTime);
        event.setTotalDurationMinutes(totalMinutes);
        event.setIsActive(false);
        event.setFinalLocationLat(trackingReqDto.getLatitude());
        event.setFinalLocationLng(trackingReqDto.getLongitude());

        ParkingEventDomain updatedEvent = parkingEventRepository.save(event);
        trackingInfo.setActiveParkingEventId(null);

        log.info("🏁 EVENTO DE ESTACIONAMIENTO FINALIZADO");
        log.info("Duración total: {} minutos", totalMinutes);
        log.info("ID del evento: {}", updatedEvent.getId());
        
        // Enviar notificación de finalización
        sendParkingAlert(updatedEvent, "VEHÍCULO_REANUDÓ_MOVIMIENTO", "Vehículo reanudó su recorrido");
    }

    private Optional<ParkingConfigDomain> getApplicableConfig(String vehicleId, Long routeId) {
        // Por ahora retornamos la primera configuración activa
        // En implementación real, se filtraría por companyId del vehículo/ruta
        return parkingConfigRepository.findAll().stream()
                .filter(config -> config.getIsActive())
                .findFirst();
    }

    private VehicleTrackingInfo getVehicleTrackingInfo(String vehicleId) {
        return vehicleTracking.computeIfAbsent(vehicleId, k -> new VehicleTrackingInfo());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Fórmula de Haversine para calcular distancia entre dos puntos GPS
        double earthRadius = 6371000; // Radio de la Tierra en metros
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return earthRadius * c;
    }

    private double calculateSpeed(double distanceMeters, long timeMillis) {
        if (timeMillis <= 0) return 0.0;
        double timeHours = timeMillis / (1000.0 * 3600.0);
        return distanceMeters / 1000.0 / timeHours; // km/h
    }

    private Double extractSpeed(TrackingReqDto trackingReqDto) {
        // Por ahora retornamos null, en implementación futura se puede extraer del JSON
        return null;
    }

    private void sendParkingAlert(ParkingEventDomain event, String type, String message) {
        try {
            // Crear objeto de alerta para enviar via WebSocket
            ParkingAlert alert = new ParkingAlert(
                event.getId(),
                event.getVehicleId(),
                event.getDriverId(),
                event.getRouteId(),
                event.getStartedAt(),
                type,
                message
            );

            // Enviar alerta via WebSocket a todos los clientes conectados
            messagingTemplate.convertAndSend("/topic/parking-alerts", alert);

            log.info("📱 ALERTA DE ESTACIONAMIENTO ENVIADA:");
            log.info("   Tipo: {}", type);
            log.info("   Mensaje: {}", message);
            log.info("   Vehículo: {}", event.getVehicleId());
            log.info("   Event ID: {}", event.getId());

        } catch (Exception e) {
            log.error("Error enviando alerta de estacionamiento via WebSocket: {}", e.getMessage(), e);
        }
    }

    // Clase interna para representar alertas de estacionamiento
    public static class ParkingAlert {
        private final Long id;
        private final String vehicleId;
        private final Long driverId;
        private final Long routeId;
        private final LocalDateTime startedAt;
        private final String type;
        private final String message;

        public ParkingAlert(Long id, String vehicleId, Long driverId, Long routeId,
                          LocalDateTime startedAt, String type, String message) {
            this.id = id;
            this.vehicleId = vehicleId;
            this.driverId = driverId;
            this.routeId = routeId;
            this.startedAt = startedAt;
            this.type = type;
            this.message = message;
        }

        // Getters
        public Long getId() { return id; }
        public String getVehicleId() { return vehicleId; }
        public Long getDriverId() { return driverId; }
        public Long getRouteId() { return routeId; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public String getType() { return type; }
        public String getMessage() { return message; }
    }

    // Clases auxiliares para tracking temporal
    private static class VehicleTrackingInfo {
        private LocationInfo lastLocation;
        private Long activeParkingEventId;
        private int accumulatedParkedMinutes = 0;

        public LocationInfo getLastLocation() { return lastLocation; }
        public void setLastLocation(LocationInfo lastLocation) { this.lastLocation = lastLocation; }
        public Long getActiveParkingEventId() { return activeParkingEventId; }
        public void setActiveParkingEventId(Long activeParkingEventId) { this.activeParkingEventId = activeParkingEventId; }
        public int getAccumulatedParkedMinutes() { return accumulatedParkedMinutes; }
        public void incrementParkedTime(Duration timeDifference) { 
            accumulatedParkedMinutes += (int) timeDifference.toMinutes(); 
        }
        public void resetParkedTime() { accumulatedParkedMinutes = 0; }
        public void updateLocation(LocationInfo newLocation) { 
            this.lastLocation = newLocation; 
        }
    }

    private static class LocationInfo {
        private final BigDecimal latitude;
        private final BigDecimal longitude;
        private final LocalDateTime timestamp;
        private final Double speed;

        public LocationInfo(BigDecimal latitude, BigDecimal longitude, LocalDateTime timestamp, Double speed) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = timestamp;
            this.speed = speed;
        }

        public BigDecimal getLatitude() { return latitude; }
        public BigDecimal getLongitude() { return longitude; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Double getSpeed() { return speed; }
    }
}