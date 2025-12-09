package com.sena.urbantracker.parking.infrastructure.persistence.model;

import com.sena.urbantracker.shared.infrastructure.persistence.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "parking_event", schema = "parking")
public class ParkingEventModel extends BaseEntity {

    @Column(name = "vehicle_id", nullable = false, length = 50)
    private String vehicleId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "total_duration_minutes")
    private Integer totalDurationMinutes;

    @Column(name = "final_location_lat")
    private BigDecimal finalLocationLat;

    @Column(name = "final_location_lng")
    private BigDecimal finalLocationLng;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = false;
}