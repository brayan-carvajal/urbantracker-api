package com.sena.urbantracker.parking.infrastructure.repository.impl;

import com.sena.urbantracker.parking.domain.entity.ParkingConfigDomain;
import com.sena.urbantracker.parking.domain.repository.ParkingConfigRepository;
import com.sena.urbantracker.parking.infrastructure.persistence.mapper.ParkingConfigPersistenceMapper;
import com.sena.urbantracker.parking.infrastructure.persistence.model.ParkingConfigModel;
import com.sena.urbantracker.parking.infrastructure.repository.jpa.ParkingConfigJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ParkingConfigRepositoryImpl implements ParkingConfigRepository {

    private final ParkingConfigJpaRepository jpaRepository;

    public ParkingConfigRepositoryImpl(ParkingConfigJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ParkingConfigDomain save(ParkingConfigDomain domain) {
        ParkingConfigModel model = ParkingConfigPersistenceMapper.toModel(domain);
        ParkingConfigModel saved = jpaRepository.save(model);
        return ParkingConfigPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<ParkingConfigDomain> findById(Long id) {
        return jpaRepository.findById(id).map(ParkingConfigPersistenceMapper::toDomain);
    }

    @Override
    public List<ParkingConfigDomain> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(ParkingConfigPersistenceMapper::toDomain)
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
    public Optional<ParkingConfigDomain> findByCompanyId(Long companyId) {
        return jpaRepository.findByCompanyId(companyId).map(ParkingConfigPersistenceMapper::toDomain);
    }

    @Override
    public List<ParkingConfigDomain> findByCompanyIdAndIsActive(Long companyId, Boolean isActive) {
        return jpaRepository.findByCompanyIdAndIsActive(companyId, isActive)
                .stream()
                .map(ParkingConfigPersistenceMapper::toDomain)
                .toList();
    }
}