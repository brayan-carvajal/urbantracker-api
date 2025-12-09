package com.sena.urbantracker.vehicles.application.dto.response;

import com.sena.urbantracker.shared.application.dto.response.ABaseResDto;
import com.sena.urbantracker.users.application.dto.response.CompanyResDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)// Comparar con todos los campos de la superclase
public class VehicleResDto extends ABaseResDto {

    private String brand;
    private String model;
    private Integer year;
    private String color;
    private Integer passengerCapacity;
    private CompanyResDto company;
    private Long companyId; // For frontend compatibility
    private String licencePlate;
    private VehicleTypeResDto vehicleType;
    private Long vehicleTypeId; // For frontend compatibility
    private String status;

    // Image availability flags (for list views - don't send BLOB data)
    private boolean hasOutboundImage;

    // BLOB data for images (only populated when specifically requested)
    private byte[] outboundImageData;
    private String outboundImageContentType;
    private byte[] returnImageData;
    private String returnImageContentType;

    // Legacy URL fields for backward compatibility
    private String outboundImageUrl;
    private String returnImageUrl;
    private boolean inService;
}
