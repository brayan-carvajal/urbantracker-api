package com.sena.urbantracker.vehicles.application.dto.request;

import com.sena.urbantracker.shared.application.dto.request.ABaseReqDto;
import com.sena.urbantracker.vehicles.domain.valueobject.VehicleStatusType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VehicleReqDto extends ABaseReqDto {
    // Las validaciones @NotNull y @NotBlank se aplican principalmente para creación
    // Para actualizaciones, estos campos pueden ser opcionales
    private Long companyId;

    private Long vehicleTypeId;

    // Patrón más flexible para placas: permite letras y números con formato flexible
    @Pattern(regexp = "^[A-Z0-9]{3,4}-?[A-Z0-9]{3,4}$", message = "La placa debe tener formato válido (ej: ABC-123, ABC123)")
    private String licencePlate;

    private String brand;

    private String model;

    private Integer year;

    private String color;

    private Integer passengerCapacity;

    private VehicleStatusType status;

    private boolean inService;

    private String outboundImageUrl;
    private String returnImageUrl;

    // Flags for image operations
    private boolean deleteOutboundImage;
}