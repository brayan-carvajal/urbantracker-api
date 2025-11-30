package com.sena.urbantracker.routes.domain.entity;

import com.sena.urbantracker.shared.application.dto.ABaseDomain;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RouteDomain extends ABaseDomain {
    private Integer numberRoute;
    private String description;
    private Double totalDistance;
    private byte[] outboundImageData;
    private String outboundImageContentType;
    private byte[] returnImageData;
    private String returnImageContentType;
    // Legacy URL fields for backward compatibility (can be removed later)
    private String outboundImageUrl;
    private String returnImageUrl;
    private Long companyId;
}