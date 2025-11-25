package com.sena.urbantracker.parking.application.dto.response;

import com.sena.urbantracker.shared.application.dto.response.ABaseResDto;
import com.sena.urbantracker.parking.domain.valueobject.ParkingStatusType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ParkingEventResDto extends ABaseResDto {

    private String vehicleId;
    private Long driverId;
    private Long routeId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer totalDurationMinutes;
    private BigDecimal finalLocationLat;
    private BigDecimal finalLocationLng;
    private ParkingStatusType status;

    // Campos calculados para la UI
    private String driverName;
    private String vehicleLicencePlate;
    private String routeNumber;
    private String companyName;
}