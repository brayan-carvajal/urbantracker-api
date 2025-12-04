package com.sena.urbantracker.parking.infrastructure.persistence.mapper;

import com.sena.urbantracker.parking.domain.entity.ParkingEventDomain;
import com.sena.urbantracker.parking.infrastructure.persistence.model.ParkingEventModel;

public class ParkingEventPersistenceMapper {

    public static ParkingEventModel toModel(ParkingEventDomain domain) {
        if (domain == null) return null;
        return ParkingEventModel.builder()
                .id(domain.getId())
                .vehicleId(domain.getVehicleId())
                .driverId(domain.getDriverId())
                .routeId(domain.getRouteId())
                .startedAt(domain.getStartedAt())
                .endedAt(domain.getEndedAt())
                .totalDurationMinutes(domain.getTotalDurationMinutes())
                .finalLocationLat(domain.getFinalLocationLat())
                .finalLocationLng(domain.getFinalLocationLng())
                .isActive(domain.getIsActive())
                .active(domain.getActive())
                .build();
    }

    public static ParkingEventDomain toDomain(ParkingEventModel model) {
        if (model == null) return null;
        return ParkingEventDomain.builder()
                .id(model.getId())
                .vehicleId(model.getVehicleId())
                .driverId(model.getDriverId())
                .routeId(model.getRouteId())
                .startedAt(model.getStartedAt())
                .endedAt(model.getEndedAt())
                .totalDurationMinutes(model.getTotalDurationMinutes())
                .finalLocationLat(model.getFinalLocationLat())
                .finalLocationLng(model.getFinalLocationLng())
                .isActive(model.getIsActive())
                .active(model.getActive())
                .build();
    }
}