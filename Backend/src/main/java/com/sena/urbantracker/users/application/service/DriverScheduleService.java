package com.sena.urbantracker.users.application.service;

import com.sena.urbantracker.users.application.dto.request.DriverScheduleReqDto;
import com.sena.urbantracker.users.application.dto.response.DriverScheduleResDto;
import com.sena.urbantracker.users.application.mapper.DriverScheduleMapper;
import com.sena.urbantracker.users.domain.entity.DriverDomain;
import com.sena.urbantracker.users.domain.entity.DriverScheduleDomain;
import com.sena.urbantracker.users.domain.repository.DriverScheduleRepository;
import com.sena.urbantracker.shared.infrastructure.exception.EntityNotFoundException;
import com.sena.urbantracker.shared.application.dto.CrudResponseDto;
import com.sena.urbantracker.shared.domain.repository.CrudOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class DriverScheduleService implements CrudOperations<DriverScheduleReqDto, DriverScheduleResDto, Long> {

    private final DriverScheduleRepository driverScheduleRepository;
    private final DriverService driverService;

    @PostConstruct
    public void init() {
        log.info("DriverScheduleService bean created");
    }

    @Override
    public CrudResponseDto<DriverScheduleResDto> create(DriverScheduleReqDto request) {
        validateScheduleTime(request);
        validateDriverExists(request.getDriverId());

        DriverScheduleDomain entity = DriverScheduleMapper.toEntity(request);
        DriverScheduleDomain saved = driverScheduleRepository.save(entity);
        return CrudResponseDto.success(DriverScheduleMapper.toDto(saved), "Horario de conductor creado correctamente");
    }

    public CrudResponseDto<List<DriverScheduleResDto>> createAll(List<DriverScheduleReqDto> request) {
        List<DriverScheduleDomain> entityList = request.stream()
            .peek(dto -> {
                validateScheduleTime(dto);
                validateDriverExists(dto.getDriverId());
            })
            .map(DriverScheduleMapper::toEntity)
            .toList();
        List<DriverScheduleDomain> savedList = driverScheduleRepository.saveAll(entityList);
        return CrudResponseDto.success(savedList.stream().map(DriverScheduleMapper::toDto).toList(), "Horarios de conductor creados correctamente");
    }

    @Override
    public CrudResponseDto<Optional<DriverScheduleResDto>> findById(Long id) {
        DriverScheduleDomain schedule = driverScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Horario de conductor no encontrado"));
        return CrudResponseDto.success(Optional.of(DriverScheduleMapper.toDto(schedule)), "Horario de conductor encontrado");
    }

    @Override
    public CrudResponseDto<List<DriverScheduleResDto>> findAll() {
        List<DriverScheduleResDto> schedules = driverScheduleRepository.findAll().stream()
                .map(DriverScheduleMapper::toDto)
                .toList();
        return CrudResponseDto.success(schedules, "Lista de horarios de conductores");
    }

    @Override
    public CrudResponseDto<DriverScheduleResDto> update(DriverScheduleReqDto request, Long id) {
        DriverScheduleDomain schedule = driverScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se puede actualizar. Horario de conductor no encontrado."));
        validateScheduleTime(request);
        validateDriverExists(request.getDriverId());

        DriverDomain driver = new DriverDomain();
        driver.setId(request.getDriverId());
        
        schedule.setDriver(driver);
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());

        DriverScheduleDomain updated = driverScheduleRepository.save(schedule);
        return CrudResponseDto.success(DriverScheduleMapper.toDto(updated), "Horario de conductor actualizado correctamente");
    }

    public CrudResponseDto<List<DriverScheduleResDto>> updateAll(List<DriverScheduleReqDto> dtos, Long id) {
        validateDriverExists(id);
        List<DriverScheduleDomain> existing = driverScheduleRepository.findByDriverId(id);

        // Índices por día
        Set<String> dtoDays = dtos.stream()
                .map(DriverScheduleReqDto::getDayOfWeek)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, DriverScheduleDomain> existingByDay = existing.stream()
                .filter(e -> e.getDayOfWeek() != null)
                .collect(Collectors.toMap(DriverScheduleDomain::getDayOfWeek, e -> e));

        // 1) updated: existentes que sí vienen en dto (coincide día)
        List<DriverScheduleDomain> updated = new ArrayList<>();
        for (DriverScheduleReqDto dto : dtos) {
            if (dto.getDayOfWeek() == null) continue;
            validateScheduleTime(dto);
            DriverScheduleDomain match = existingByDay.get(dto.getDayOfWeek());
            if (match != null) {
                match.setStartTime(dto.getStartTime());
                match.setEndTime(dto.getEndTime());
                updated.add(match);
            }
        }

        List<DriverScheduleDomain> missingInDto = existing.stream()
                .filter(e -> e.getDayOfWeek() != null && !dtoDays.contains(e.getDayOfWeek()))
                .toList();

        List<DriverScheduleDomain> newInDto = dtos.stream()
                .filter(d -> d.getDayOfWeek() != null && !existingByDay.containsKey(d.getDayOfWeek()))
                .peek(this::validateScheduleTime)
                .map(d -> {
                    DriverScheduleDomain ne = DriverScheduleMapper.toEntity(d);
                    DriverDomain driverRef = new DriverDomain();
                    driverRef.setId(id);
                    ne.setDriver(driverRef);
                    return ne;
                })
                .toList();

        if (!updated.isEmpty()) driverScheduleRepository.saveAll(updated);
        if (!missingInDto.isEmpty()) driverScheduleRepository.deleteAll(missingInDto);
        if (!newInDto.isEmpty()) driverScheduleRepository.saveAll(newInDto);

        List<DriverScheduleResDto> result = driverScheduleRepository.findByDriverId(id).stream()
                .map(DriverScheduleMapper::toDto)
                .toList();

        return CrudResponseDto.success(result, "Horarios actualizados correctamente");
    }

    @Override
    public CrudResponseDto<DriverScheduleResDto> deleteById(Long id) {
        if (!driverScheduleRepository.existsById(id)) {
            throw new EntityNotFoundException("Horario de conductor no encontrado");
        }
        driverScheduleRepository.deleteById(id);
        return CrudResponseDto.success(null, "Horario de conductor eliminado correctamente");
    }

    private void validateScheduleTime(DriverScheduleReqDto request) {
        if (request.getStartTime() == null || request.getEndTime() == null || !request.getStartTime().before(request.getEndTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }
    }

    private void validateDriverExists(Long driverId) {
        if (!driverService.existsById(driverId).getData()) {
            throw new EntityNotFoundException("El conductor con ID " + driverId + " no existe");
        }
    }

    @Override
    public CrudResponseDto<Boolean> existsById(Long id) {
        return CrudResponseDto.success(driverScheduleRepository.existsById(id), "Verificación de existencia completada");
    }

    @Override
    public CrudResponseDto<DriverScheduleResDto> activateById(Long id) {
        throw new UnsupportedOperationException("Los horarios de conductores no soportan activación");
    }

    @Override
    public CrudResponseDto<DriverScheduleResDto> deactivateById(Long id) {
        throw new UnsupportedOperationException("Los horarios de conductores no soportan desactivación");
    }

    public CrudResponseDto<List<DriverScheduleResDto>> findByDriverId(Long driverId) {
        if (!driverService.existsById(driverId).getData()) {
            throw new EntityNotFoundException("El conductor con ID " + driverId + " no existe");
        }
        List<DriverScheduleResDto> schedules = driverScheduleRepository.findByDriverId(driverId).stream()
                .map(DriverScheduleMapper::toDto)
                .toList();
        return CrudResponseDto.success(schedules, "Horarios del conductor encontrados");
    }

    @org.springframework.transaction.annotation.Transactional
    public CrudResponseDto<Void> deleteByDriverId(Long driverId) {
        if (!driverService.existsById(driverId).getData()) {
            throw new EntityNotFoundException("El conductor con ID " + driverId + " no existe");
        }
        driverScheduleRepository.deleteByDriverId(driverId);
        return CrudResponseDto.success(null, "Horarios del conductor eliminados correctamente");
    }
}