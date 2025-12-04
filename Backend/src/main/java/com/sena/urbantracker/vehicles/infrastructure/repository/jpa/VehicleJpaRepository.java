package com.sena.urbantracker.vehicles.infrastructure.repository.jpa;

import com.sena.urbantracker.vehicles.infrastructure.persistence.model.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VehicleJpaRepository extends JpaRepository<VehicleModel, Long> {

    boolean existsByLicencePlate(String licencePlate);

    @Query("SELECT v FROM VehicleModel v LEFT JOIN FETCH v.company LEFT JOIN FETCH v.vehicleType WHERE v.id = :id")
    Optional<VehicleModel> findByIdWithRelations(@Param("id") Long id);
}