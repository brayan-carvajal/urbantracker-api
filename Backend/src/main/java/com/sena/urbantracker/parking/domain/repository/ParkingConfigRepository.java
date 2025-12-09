package com.sena.urbantracker.parking.domain.repository;

import com.sena.urbantracker.parking.domain.entity.ParkingConfigDomain;

import java.util.List;
import java.util.Optional;

public interface ParkingConfigRepository {

    ParkingConfigDomain save(ParkingConfigDomain domain);

    Optional<ParkingConfigDomain> findById(Long id);

    List<ParkingConfigDomain> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

    Optional<ParkingConfigDomain> findByCompanyId(Long companyId);

    List<ParkingConfigDomain> findByCompanyIdAndIsActive(Long companyId, Boolean isActive);
}