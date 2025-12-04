package com.sena.urbantracker.users.infrastructure.repository.jpa;

import com.sena.urbantracker.users.infrastructure.persistence.model.DriverScheduleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverScheduleJpaRepository extends JpaRepository<DriverScheduleModel, Long> {
    List<DriverScheduleModel> findByDriverId(Long driverId);
    void deleteByDriverId(Long driverId);
}