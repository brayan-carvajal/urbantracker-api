package com.sena.urbantracker.parking.application.service;

import com.sena.urbantracker.parking.application.dto.request.ParkingConfigReqDto;
import com.sena.urbantracker.parking.application.dto.response.ParkingConfigResDto;
import com.sena.urbantracker.parking.application.mapper.ParkingConfigMapper;
import com.sena.urbantracker.parking.domain.entity.ParkingConfigDomain;
import com.sena.urbantracker.parking.domain.repository.ParkingConfigRepository;
import com.sena.urbantracker.shared.infrastructure.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingConfigService {

    private final ParkingConfigRepository parkingConfigRepository;

    public List<ParkingConfigResDto> findAll() {
        return parkingConfigRepository.findAll()
                .stream()
                .map(ParkingConfigMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<ParkingConfigResDto> findById(Long id) {
        return parkingConfigRepository.findById(id)
                .map(ParkingConfigMapper::toDto);
    }

    public ParkingConfigResDto create(ParkingConfigReqDto request) {
        ParkingConfigDomain entity = ParkingConfigMapper.toEntity(request);
        ParkingConfigDomain saved = parkingConfigRepository.save(entity);
        return ParkingConfigMapper.toDto(saved);
    }

    public ParkingConfigResDto update(Long id, ParkingConfigReqDto request) {
        ParkingConfigDomain existing = parkingConfigRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Configuración de estacionamiento con id " + id + " no encontrada"));

        existing.setCompanyId(request.getCompanyId());
        existing.setMinTimeMinutes(request.getMinTimeMinutes());
        existing.setMaxDistanceMeters(request.getMaxDistanceMeters());
        existing.setMaxSpeedKmh(request.getMaxSpeedKmh());
        existing.setIsActive(request.getIsActive());

        ParkingConfigDomain updated = parkingConfigRepository.save(existing);
        return ParkingConfigMapper.toDto(updated);
    }

    public void delete(Long id) {
        if (!parkingConfigRepository.existsById(id)) {
            throw new EntityNotFoundException("Configuración de estacionamiento con id " + id + " no encontrada");
        }
        parkingConfigRepository.deleteById(id);
    }

    public Optional<ParkingConfigResDto> findByCompanyId(Long companyId) {
        return parkingConfigRepository.findByCompanyId(companyId)
                .map(ParkingConfigMapper::toDto);
    }

    public List<ParkingConfigResDto> findByCompanyIdAndIsActive(Long companyId, Boolean isActive) {
        return parkingConfigRepository.findByCompanyIdAndIsActive(companyId, isActive)
                .stream()
                .map(ParkingConfigMapper::toDto)
                .collect(Collectors.toList());
    }
}