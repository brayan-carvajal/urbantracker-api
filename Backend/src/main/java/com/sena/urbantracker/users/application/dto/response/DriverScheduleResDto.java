package com.sena.urbantracker.users.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sena.urbantracker.shared.application.dto.response.ABaseResDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.sql.Time;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DriverScheduleResDto extends ABaseResDto {
    private Long driverId;
    private String dayOfWeek;
    @JsonFormat(pattern = "HH:mm:ss")
    private Time startTime;
    @JsonFormat(pattern = "HH:mm:ss")
    private Time endTime;
}