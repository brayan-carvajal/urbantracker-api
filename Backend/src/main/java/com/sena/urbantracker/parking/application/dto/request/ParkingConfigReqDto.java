package com.sena.urbantracker.parking.application.dto.request;

import com.sena.urbantracker.shared.application.dto.request.ABaseReqDto;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ParkingConfigReqDto extends ABaseReqDto {

    @NotNull(message = "El ID de la compañía es obligatorio")
    private Long companyId;

    @NotNull(message = "El tiempo mínimo en minutos es obligatorio")
    @Min(value = 1, message = "El tiempo mínimo debe ser al menos 1 minuto")
    @Max(value = 480, message = "El tiempo mínimo no puede exceder 8 horas")
    private Integer minTimeMinutes;

    @NotNull(message = "La distancia máxima en metros es obligatoria")
    @DecimalMin(value = "1.0", message = "La distancia máxima debe ser al menos 1 metro")
    @DecimalMax(value = "1000.0", message = "La distancia máxima no puede exceder 1000 metros")
    private Double maxDistanceMeters;

    @NotNull(message = "La velocidad máxima en km/h es obligatoria")
    @DecimalMin(value = "0.1", message = "La velocidad máxima debe ser al menos 0.1 km/h")
    @DecimalMax(value = "50.0", message = "La velocidad máxima no puede exceder 50 km/h")
    private Double maxSpeedKmh;

    private Boolean isActive = true;
}