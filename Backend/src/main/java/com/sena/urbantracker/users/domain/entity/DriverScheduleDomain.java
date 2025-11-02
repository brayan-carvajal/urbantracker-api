package com.sena.urbantracker.users.domain.entity;

import com.sena.urbantracker.shared.application.dto.ABaseDomain;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.Time;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DriverScheduleDomain extends ABaseDomain {
    private DriverDomain driver;
    private String dayOfWeek;
    private Time startTime;
    private Time endTime;
}