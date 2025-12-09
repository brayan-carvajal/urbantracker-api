package com.sena.urbantracker.parking.infrastructure.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sena.urbantracker.monitoring.application.dto.request.TrackingReqDto;
import com.sena.urbantracker.parking.application.service.ParkingDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingDetectionListener {

    @Autowired
    private ParkingDetectionService parkingDetectionService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Listener adicional para el canal MQTT que procesa ubicaciones para detección de estacionamiento
     */
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleParkingDetection(Message<?> message) {
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        String payload = message.getPayload().toString();

        log.debug("🚗 Procesando ubicación para detección de estacionamiento | Topic: {} | Payload: {}", topic, payload);

        // Solo procesar ubicaciones de rutas o vehículos
        if (topic.startsWith("routes/") || topic.startsWith("vehicles/")) {
            try {
                TrackingReqDto telemetry = objectMapper.readValue(payload, TrackingReqDto.class);
                
                // Enviar al servicio de detección de estacionamiento
                parkingDetectionService.processLocationUpdate(telemetry);
                
                log.debug("✅ Ubicación procesada para detección de estacionamiento - VehicleId: {}", telemetry.getVehicleId());
                
            } catch (Exception e) {
                log.error("❌ Error procesando ubicación para estacionamiento | Topic: {} | Payload: {}", topic, payload, e);
            }
        }
    }
}