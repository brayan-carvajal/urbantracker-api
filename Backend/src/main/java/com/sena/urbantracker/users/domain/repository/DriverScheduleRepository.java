package com.sena.urbantracker.users.domain.repository;

import com.sena.urbantracker.users.domain.entity.DriverScheduleDomain;

import java.util.List;
import java.util.Optional;

public interface DriverScheduleRepository {

    DriverScheduleDomain save(DriverScheduleDomain domain);

    Optional<DriverScheduleDomain> findById(Long id);

    List<DriverScheduleDomain> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

    List<DriverScheduleDomain> saveAll(List<DriverScheduleDomain> domainList);

    List<DriverScheduleDomain> findByDriverId(Long driverId);

    void deleteAll(List<DriverScheduleDomain> schedules);

    void deleteByDriverId(Long driverId);
}