package com.sena.urbantracker.parking.infrastructure.repository.jpa;

import com.sena.urbantracker.parking.infrastructure.persistence.model.ParkingEventModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkingEventJpaRepository extends JpaRepository<ParkingEventModel, Long> {

    Optional<ParkingEventModel> findByVehicleIdAndIsActive(String vehicleId, Boolean isActive);

    @Query("SELECT e FROM ParkingEventModel e WHERE e.vehicleId = :vehicleId AND e.startedAt >= :startDate AND e.startedAt <= :endDate")
    List<ParkingEventModel> findByVehicleIdAndDateRange(@Param("vehicleId") String vehicleId, 
                                                       @Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM ParkingEventModel e WHERE e.routeId IN (SELECT r.id FROM RouteModel r WHERE r.company.id = :companyId) AND e.startedAt >= :startDate AND e.startedAt <= :endDate")
    List<ParkingEventModel> findByCompanyIdAndDateRange(@Param("companyId") Long companyId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM ParkingEventModel e WHERE e.driverId = :driverId AND e.startedAt >= :startDate AND e.startedAt <= :endDate")
    List<ParkingEventModel> findByDriverIdAndDateRange(@Param("driverId") Long driverId, 
                                                       @Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
}