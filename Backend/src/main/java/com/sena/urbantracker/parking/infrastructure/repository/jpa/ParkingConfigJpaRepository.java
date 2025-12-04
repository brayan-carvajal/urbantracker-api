package com.sena.urbantracker.parking.infrastructure.repository.jpa;

import com.sena.urbantracker.parking.infrastructure.persistence.model.ParkingConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingConfigJpaRepository extends JpaRepository<ParkingConfigModel, Long> {

    Optional<ParkingConfigModel> findByCompanyId(Long companyId);

    List<ParkingConfigModel> findByCompanyIdAndIsActive(Long companyId, Boolean isActive);
}