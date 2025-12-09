package com.sena.urbantracker.parking.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ParkingStatsDto {
    private int totalEvents;
    private int activeEvents;
    private int eventsToday;
    private int eventsThisWeek;
    private double averageParkingDurationMinutes;
    private LocalDateTime lastEventTime;
    private String mostFrequentParkingVehicle;
    private String mostFrequentParkingDriver;
}