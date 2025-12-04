package com.sena.urbantracker.parking.application.mapper;

import com.sena.urbantracker.parking.application.dto.response.ParkingEventResDto;
import com.sena.urbantracker.parking.domain.entity.ParkingEventDomain;
import com.sena.urbantracker.parking.domain.valueobject.ParkingStatusType;

public class ParkingEventMapper {

    public static ParkingEventResDto toDto(ParkingEventDomain entity) {
        if (entity == null) return null;
        
        ParkingStatusType status = entity.getIsActive() != null && entity.getIsActive() 
                ? ParkingStatusType.ACTIVE 
                : ParkingStatusType.ENDED;
        
        return ParkingEventResDto.builder()
                .id(entity.getId())
                .vehicleId(entity.getVehicleId())
                .driverId(entity.getDriverId())
                .routeId(entity.getRouteId())
                .startedAt(entity.getStartedAt())
                .endedAt(entity.getEndedAt())
                .totalDurationMinutes(entity.getTotalDurationMinutes())
                .finalLocationLat(entity.getFinalLocationLat())
                .finalLocationLng(entity.getFinalLocationLng())
                .status(status)
                .active(entity.getActive())
                .build();
    }

    public static ParkingEventDomain toEntity(ParkingEventResDto dto) {
        if (dto == null) return null;
        
        Boolean isActive = dto.getStatus() == ParkingStatusType.ACTIVE;
        
        return ParkingEventDomain.builder()
                .vehicleId(dto.getVehicleId())
                .driverId(dto.getDriverId())
                .routeId(dto.getRouteId())
                .startedAt(dto.getStartedAt())
                .endedAt(dto.getEndedAt())
                .totalDurationMinutes(dto.getTotalDurationMinutes())
                .finalLocationLat(dto.getFinalLocationLat())
                .finalLocationLng(dto.getFinalLocationLng())
                .isActive(isActive)
                .active(dto.getActive())
                .build();
    }
}