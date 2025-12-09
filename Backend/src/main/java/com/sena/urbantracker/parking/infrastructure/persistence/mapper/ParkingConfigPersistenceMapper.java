package com.sena.urbantracker.parking.infrastructure.persistence.mapper;

import com.sena.urbantracker.parking.domain.entity.ParkingConfigDomain;
import com.sena.urbantracker.parking.infrastructure.persistence.model.ParkingConfigModel;

public class ParkingConfigPersistenceMapper {

    public static ParkingConfigModel toModel(ParkingConfigDomain domain) {
        if (domain == null) return null;
        return ParkingConfigModel.builder()
                .id(domain.getId())
                .companyId(domain.getCompanyId())
                .minTimeMinutes(domain.getMinTimeMinutes())
                .maxDistanceMeters(domain.getMaxDistanceMeters())
                .maxSpeedKmh(domain.getMaxSpeedKmh())
                .isActive(domain.getIsActive())
                .active(domain.getActive())
                .build();
    }

    public static ParkingConfigDomain toDomain(ParkingConfigModel model) {
        if (model == null) return null;
        return ParkingConfigDomain.builder()
                .id(model.getId())
                .companyId(model.getCompanyId())
                .minTimeMinutes(model.getMinTimeMinutes())
                .maxDistanceMeters(model.getMaxDistanceMeters())
                .maxSpeedKmh(model.getMaxSpeedKmh())
                .isActive(model.getIsActive())
                .active(model.getActive())
                .build();
    }
}