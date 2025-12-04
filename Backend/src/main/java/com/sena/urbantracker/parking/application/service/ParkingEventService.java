package com.sena.urbantracker.parking.application.service;

import com.sena.urbantracker.parking.application.dto.response.ParkingEventResDto;
import com.sena.urbantracker.parking.application.dto.response.ParkingStatsDto;
import com.sena.urbantracker.parking.application.mapper.ParkingEventMapper;
import com.sena.urbantracker.parking.domain.entity.ParkingEventDomain;
import com.sena.urbantracker.parking.domain.repository.ParkingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingEventService {

    private final ParkingEventRepository parkingEventRepository;

    public List<ParkingEventResDto> findAll() {
        return parkingEventRepository.findAll()
                .stream()
                .map(ParkingEventMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ParkingEventResDto> findActiveEvents() {
        return parkingEventRepository.findAll()
                .stream()
                .filter(event -> event.getIsActive() != null && event.getIsActive())
                .map(ParkingEventMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ParkingEventResDto> findByVehicleId(String vehicleId) {
        return parkingEventRepository.findAll()
                .stream()
                .filter(event -> event.getVehicleId().equals(vehicleId))
                .map(ParkingEventMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ParkingEventResDto> findByVehicleIdAndDateRange(String vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        return parkingEventRepository.findByVehicleIdAndDateRange(vehicleId, startDate, endDate)
                .stream()
                .map(ParkingEventMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ParkingEventResDto> findByDriverId(Long driverId) {
        return parkingEventRepository.findAll()
                .stream()
                .filter(event -> event.getDriverId() != null && event.getDriverId().equals(driverId))
                .map(ParkingEventMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ParkingEventResDto> findByDriverIdAndDateRange(Long driverId, LocalDateTime startDate, LocalDateTime endDate) {
        return parkingEventRepository.findByDriverIdAndDateRange(driverId, startDate, endDate)
                .stream()
                .map(ParkingEventMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ParkingEventResDto> findByCompanyId(Long companyId) {
        return parkingEventRepository.findAll()
                .stream()
                .filter(event -> event.getRouteId() != null)
                .map(ParkingEventMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ParkingEventResDto> findByCompanyIdAndDateRange(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        return parkingEventRepository.findByCompanyIdAndDateRange(companyId, startDate, endDate)
                .stream()
                .map(ParkingEventMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ParkingEventResDto> findRecentEvents(LocalDateTime since) {
        return parkingEventRepository.findAll()
                .stream()
                .filter(event -> event.getStartedAt().isAfter(since))
                .map(ParkingEventMapper::toDto)
                .collect(Collectors.toList());
    }

    public ParkingStatsDto getParkingStats() {
        List<ParkingEventDomain> allEvents = parkingEventRepository.findAll();

        int totalEvents = allEvents.size();
        int activeEvents = (int) allEvents.stream()
                .filter(event -> event.getIsActive() != null && event.getIsActive())
                .count();

        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        int eventsToday = (int) allEvents.stream()
                .filter(event -> event.getStartedAt().toLocalDate().isEqual(today.toLocalDate()))
                .count();

        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        int eventsThisWeek = (int) allEvents.stream()
                .filter(event -> event.getStartedAt().isAfter(weekStart))
                .count();

        double averageDuration = allEvents.stream()
                .filter(event -> event.getTotalDurationMinutes() != null)
                .mapToInt(ParkingEventDomain::getTotalDurationMinutes)
                .average()
                .orElse(0.0);

        LocalDateTime lastEventTime = allEvents.stream()
                .map(ParkingEventDomain::getStartedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Estadísticas básicas por ahora
        return ParkingStatsDto.builder()
                .totalEvents(totalEvents)
                .activeEvents(activeEvents)
                .eventsToday(eventsToday)
                .eventsThisWeek(eventsThisWeek)
                .averageParkingDurationMinutes(averageDuration)
                .lastEventTime(lastEventTime)
                .mostFrequentParkingVehicle(null) // Implementar más tarde
                .mostFrequentParkingDriver(null)   // Implementar más tarde
                .build();
    }
}