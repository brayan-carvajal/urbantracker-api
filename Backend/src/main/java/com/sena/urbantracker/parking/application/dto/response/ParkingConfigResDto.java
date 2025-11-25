package com.sena.urbantracker.parking.application.dto.response;

import com.sena.urbantracker.shared.application.dto.response.ABaseResDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ParkingConfigResDto extends ABaseResDto {

    private Long companyId;
    private String companyName;
    private Integer minTimeMinutes;
    private Double maxDistanceMeters;
    private Double maxSpeedKmh;
    private Boolean isActive;
}