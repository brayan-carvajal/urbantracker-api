package com.sena.urbantracker.parking.domain.repository;

import com.sena.urbantracker.parking.domain.entity.ParkingEventDomain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkingEventRepository {

    ParkingEventDomain save(ParkingEventDomain domain);

    Optional<ParkingEventDomain> findById(Long id);

    List<ParkingEventDomain> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

    Optional<ParkingEventDomain> findActiveEventByVehicleId(String vehicleId);

    List<ParkingEventDomain> findByVehicleIdAndDateRange(String vehicleId, LocalDateTime startDate, LocalDateTime endDate);

    List<ParkingEventDomain> findByCompanyIdAndDateRange(Long companyId, LocalDateTime startDate, LocalDateTime endDate);

    List<ParkingEventDomain> findByDriverIdAndDateRange(Long driverId, LocalDateTime startDate, LocalDateTime endDate);
}