package com.sena.urbantracker.users.application.mapper;

import com.sena.urbantracker.users.application.dto.request.DriverScheduleReqDto;
import com.sena.urbantracker.users.application.dto.response.DriverScheduleResDto;
import com.sena.urbantracker.users.domain.entity.DriverDomain;
import com.sena.urbantracker.users.domain.entity.DriverScheduleDomain;

public class DriverScheduleMapper {

    public static DriverScheduleResDto toDto(DriverScheduleDomain entity) {
        if (entity == null) return null;

        // Creamos el DTO solo con la información básica del horario
        return DriverScheduleResDto.builder()
                .id(entity.getId())
                .driverId(entity.getDriver() != null ? entity.getDriver().getId() : null)
                .dayOfWeek(entity.getDayOfWeek())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static DriverScheduleDomain toEntity(DriverScheduleReqDto dto) {
        if (dto == null) return null;
        return DriverScheduleDomain.builder()
                .driver(DriverDomain.builder().id(dto.getDriverId()).build())
                .dayOfWeek(dto.getDayOfWeek())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }
}