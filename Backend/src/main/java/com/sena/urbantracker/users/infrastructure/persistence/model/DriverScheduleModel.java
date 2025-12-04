package com.sena.urbantracker.users.infrastructure.persistence.model;

import com.sena.urbantracker.shared.infrastructure.persistence.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.Time;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "driver_schedule", schema = "users")
public class DriverScheduleModel extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverModel driver;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private Time startTime;

    @Column(name = "end_time", nullable = false)
    private Time endTime;
}