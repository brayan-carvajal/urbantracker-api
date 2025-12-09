package com.sena.urbantracker.parking.infrastructure.repository.impl;

import com.sena.urbantracker.parking.domain.entity.ParkingEventDomain;
import com.sena.urbantracker.parking.domain.repository.ParkingEventRepository;
import com.sena.urbantracker.parking.infrastructure.persistence.mapper.ParkingEventPersistenceMapper;
import com.sena.urbantracker.parking.infrastructure.persistence.model.ParkingEventModel;
import com.sena.urbantracker.parking.infrastructure.repository.jpa.ParkingEventJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ParkingEventRepositoryImpl implements ParkingEventRepository {

    private final ParkingEventJpaRepository jpaRepository;

    public ParkingEventRepositoryImpl(ParkingEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ParkingEventDomain save(ParkingEventDomain domain) {
        ParkingEventModel model = ParkingEventPersistenceMapper.toModel(domain);
        ParkingEventModel saved = jpaRepository.save(model);
        return ParkingEventPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<ParkingEventDomain> findById(Long id) {
        return jpaRepository.findById(id).map(ParkingEventPersistenceMapper::toDomain);
    }

    @Override
    public List<ParkingEventDomain> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(ParkingEventPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Optional<ParkingEventDomain> findActiveEventByVehicleId(String vehicleId) {
        return jpaRepository.findByVehicleIdAndIsActive(vehicleId, true)
                .map(ParkingEventPersistenceMapper::toDomain);
    }

    @Override
    public List<ParkingEventDomain> findByVehicleIdAndDateRange(String vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByVehicleIdAndDateRange(vehicleId, startDate, endDate)
                .stream()
                .map(ParkingEventPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<ParkingEventDomain> findByCompanyIdAndDateRange(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByCompanyIdAndDateRange(companyId, startDate, endDate)
                .stream()
                .map(ParkingEventPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<ParkingEventDomain> findByDriverIdAndDateRange(Long driverId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByDriverIdAndDateRange(driverId, startDate, endDate)
                .stream()
                .map(ParkingEventPersistenceMapper::toDomain)
                .toList();
    }
}