package com.sena.urbantracker.users.infrastructure.repository.impl;

import com.sena.urbantracker.users.domain.entity.DriverScheduleDomain;
import com.sena.urbantracker.users.domain.repository.DriverScheduleRepository;
import com.sena.urbantracker.users.infrastructure.persistence.mapper.DriverSchedulePersistenceMapper;
import com.sena.urbantracker.users.infrastructure.repository.jpa.DriverScheduleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DriverScheduleRepositoryImpl implements DriverScheduleRepository {

    private final DriverScheduleJpaRepository jpaRepository;

    @Override
    public DriverScheduleDomain save(DriverScheduleDomain domain) {
        return DriverSchedulePersistenceMapper.toDomain(
            jpaRepository.save(DriverSchedulePersistenceMapper.toModel(domain))
        );
    }

    @Override
    public Optional<DriverScheduleDomain> findById(Long id) {
        return jpaRepository.findById(id)
                .map(DriverSchedulePersistenceMapper::toDomain);
    }

    @Override
    public List<DriverScheduleDomain> findAll() {
        return jpaRepository.findAll().stream()
                .map(DriverSchedulePersistenceMapper::toDomain)
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
    public List<DriverScheduleDomain> saveAll(List<DriverScheduleDomain> domainList) {
        return jpaRepository.saveAll(
            domainList.stream()
                .map(DriverSchedulePersistenceMapper::toModel)
                .toList()
        ).stream()
            .map(DriverSchedulePersistenceMapper::toDomain)
            .toList();
    }

    @Override
    public List<DriverScheduleDomain> findByDriverId(Long driverId) {
        return jpaRepository.findByDriverId(driverId).stream()
                .map(DriverSchedulePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteAll(List<DriverScheduleDomain> schedules) {
        jpaRepository.deleteAll(
            schedules.stream()
                .map(DriverSchedulePersistenceMapper::toModel)
                .toList()
        );
    }

    @Override
    public void deleteByDriverId(Long driverId) {
        jpaRepository.deleteByDriverId(driverId);
    }
}