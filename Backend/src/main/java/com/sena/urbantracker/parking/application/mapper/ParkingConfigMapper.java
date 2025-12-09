package com.sena.urbantracker.parking.application.mapper;

import com.sena.urbantracker.parking.application.dto.request.ParkingConfigReqDto;
import com.sena.urbantracker.parking.application.dto.response.ParkingConfigResDto;
import com.sena.urbantracker.parking.domain.entity.ParkingConfigDomain;

public class ParkingConfigMapper {

    public static ParkingConfigResDto toDto(ParkingConfigDomain entity) {
        if (entity == null) return null;
        return ParkingConfigResDto.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .minTimeMinutes(entity.getMinTimeMinutes())
                .maxDistanceMeters(entity.getMaxDistanceMeters())
                .maxSpeedKmh(entity.getMaxSpeedKmh())
                .isActive(entity.getIsActive())
                .active(entity.getActive())
                .build();
    }

    public static ParkingConfigDomain toEntity(ParkingConfigReqDto dto) {
        if (dto == null) return null;
        return ParkingConfigDomain.builder()
                .companyId(dto.getCompanyId())
                .minTimeMinutes(dto.getMinTimeMinutes())
                .maxDistanceMeters(dto.getMaxDistanceMeters())
                .maxSpeedKmh(dto.getMaxSpeedKmh())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .active(true) // Default value
                .build();
    }
}