package com.sena.urbantracker.parking.infrastructure.controller;

import com.sena.urbantracker.parking.application.dto.request.ParkingConfigReqDto;
import com.sena.urbantracker.parking.application.dto.response.ParkingConfigResDto;
import com.sena.urbantracker.parking.application.service.ParkingConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/parking/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ParkingConfigController {

    private final ParkingConfigService parkingConfigService;

    /**
     * Obtiene todas las configuraciones de estacionamiento
     */
    @GetMapping
    public ResponseEntity<List<ParkingConfigResDto>> getAllConfigs() {
        List<ParkingConfigResDto> configs = parkingConfigService.findAll();
        return ResponseEntity.ok(configs);
    }

    /**
     * Obtiene una configuración por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ParkingConfigResDto> getConfigById(@PathVariable Long id) {
        Optional<ParkingConfigResDto> config = parkingConfigService.findById(id);
        return config.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene configuración por compañía
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ParkingConfigResDto> getConfigByCompanyId(@PathVariable Long companyId) {
        Optional<ParkingConfigResDto> config = parkingConfigService.findByCompanyId(companyId);
        return config.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea una nueva configuración de estacionamiento
     */
    @PostMapping
    public ResponseEntity<ParkingConfigResDto> createConfig(@RequestBody ParkingConfigReqDto request) {
        ParkingConfigResDto created = parkingConfigService.create(request);
        return ResponseEntity.ok(created);
    }

    /**
     * Actualiza una configuración existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ParkingConfigResDto> updateConfig(@PathVariable Long id, @RequestBody ParkingConfigReqDto request) {
        ParkingConfigResDto updated = parkingConfigService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina una configuración
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        parkingConfigService.delete(id);
        return ResponseEntity.noContent().build();
    }
}