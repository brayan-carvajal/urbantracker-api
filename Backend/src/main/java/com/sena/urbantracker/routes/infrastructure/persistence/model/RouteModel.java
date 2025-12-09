package com.sena.urbantracker.routes.infrastructure.persistence.model;

import com.sena.urbantracker.shared.infrastructure.persistence.model.BaseEntity;
import com.sena.urbantracker.users.infrastructure.persistence.model.CompanyModel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "route", schema = "routes")
public class RouteModel extends BaseEntity {

    @Column(name = "number_route",nullable = false, unique = true)
    private Integer numberRoute;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_distance")
    private Double totalDistance;

    @Lob
    @Column(name = "outbound_image_data")
    private byte[] outboundImageData;

    @Column(name = "outbound_image_content_type", length = 100)
    private String outboundImageContentType;

    @Lob
    @Column(name = "return_image_data")
    private byte[] returnImageData;

    @Column(name = "return_image_content_type", length = 100)
    private String returnImageContentType;

    // Legacy URL fields for backward compatibility (can be removed later)
    @Column(name = "outbound_image_url", length = 500)
    private String outboundImageUrl;

    @Column(name = "return_image_url", length = 500)
    private String returnImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = true)
    private CompanyModel company;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    private List<RouteWaypointModel> routeWaypoints;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    private List<RouteScheduleModel> routeSchedules;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    private List<RouteAssignmentModel> routeAssignments;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    private List<RouteTrajectoryModel> routeTrajectories;
}