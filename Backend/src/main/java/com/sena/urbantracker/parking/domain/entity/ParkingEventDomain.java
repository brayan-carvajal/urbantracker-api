package com.sena.urbantracker.parking.domain.entity;

import com.sena.urbantracker.shared.application.dto.ABaseDomain;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ParkingEventDomain extends ABaseDomain {
    private String vehicleId;
    private Long driverId;
    private Long routeId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer totalDurationMinutes;
    private BigDecimal finalLocationLat;
    private BigDecimal finalLocationLng;
    private Boolean isActive;
}