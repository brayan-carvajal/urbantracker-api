package com.sena.urbantracker.routes.infrastructure.persistence.mapper;

import com.sena.urbantracker.routes.domain.entity.RouteDomain;
import com.sena.urbantracker.routes.infrastructure.persistence.model.RouteModel;

public class RoutePersistenceMapper {

    public static RouteModel toModel(RouteDomain domain) {
        if (domain == null) return null;
        return RouteModel.builder()
                .id(domain.getId())
                .numberRoute(domain.getNumberRoute())
                .description(domain.getDescription())
                .totalDistance(domain.getTotalDistance())
                .outboundImageUrl(domain.getOutboundImageUrl())
                .returnImageUrl(domain.getReturnImageUrl())
                .active(domain.getActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .company(domain.getCompanyId() != null ? 
                    com.sena.urbantracker.users.infrastructure.persistence.model.CompanyModel.builder()
                        .id(domain.getCompanyId()).build() : null)
                .build();
    }

    public static RouteDomain toDomain(RouteModel model) {
        if (model == null) return null;
        return RouteDomain.builder()
                .id(model.getId())
                .numberRoute(model.getNumberRoute())
                .description(model.getDescription())
                .totalDistance(model.getTotalDistance())
                .outboundImageUrl(model.getOutboundImageUrl())
                .returnImageUrl(model.getReturnImageUrl())
                .active(model.getActive())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .companyId(model.getCompany() != null ? model.getCompany().getId() : null)
                .build();
    }
}