package com.sena.urbantracker.parking.domain.entity;

import com.sena.urbantracker.shared.application.dto.ABaseDomain;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ParkingConfigDomain extends ABaseDomain {
    private Long companyId;
    private Integer minTimeMinutes;
    private Double maxDistanceMeters;
    private Double maxSpeedKmh;
    private Boolean isActive;
}