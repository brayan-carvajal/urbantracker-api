package com.sena.urbantracker.users.infrastructure.persistence.mapper;

import com.sena.urbantracker.users.domain.entity.DriverScheduleDomain;
import com.sena.urbantracker.users.infrastructure.persistence.model.DriverScheduleModel;

public class DriverSchedulePersistenceMapper {

    private DriverSchedulePersistenceMapper() {}

    public static DriverScheduleModel toModel(DriverScheduleDomain domain) {
        if (domain == null) return null;
        return DriverScheduleModel.builder()
                .id(domain.getId())
                .driver(DriverPersistenceMapper.toModel(domain.getDriver()))
                .dayOfWeek(domain.getDayOfWeek())
                .startTime(domain.getStartTime())
                .endTime(domain.getEndTime())
                .build();
    }

    public static DriverScheduleDomain toDomain(DriverScheduleModel model) {
        if (model == null) return null;
        return DriverScheduleDomain.builder()
                .id(model.getId())
                .driver(DriverPersistenceMapper.toDomain(model.getDriver()))
                .dayOfWeek(model.getDayOfWeek())
                .startTime(model.getStartTime())
                .endTime(model.getEndTime())
                .build();
    }
}