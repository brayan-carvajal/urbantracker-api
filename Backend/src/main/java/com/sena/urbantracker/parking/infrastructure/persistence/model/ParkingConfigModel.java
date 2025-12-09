package com.sena.urbantracker.parking.infrastructure.persistence.model;

import com.sena.urbantracker.shared.infrastructure.persistence.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "parking_config", schema = "parking")
public class ParkingConfigModel extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "min_time_minutes", nullable = false)
    private Integer minTimeMinutes;

    @Column(name = "max_distance_meters", nullable = false)
    private Double maxDistanceMeters;

    @Column(name = "max_speed_kmh", nullable = false)
    private Double maxSpeedKmh;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}