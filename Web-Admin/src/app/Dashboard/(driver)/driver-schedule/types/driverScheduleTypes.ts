import { Driver } from '../../../drivers/types/driverTypes';

export interface DriverScheduleRequest {
  driverId: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
}

export interface DriverSchedule {
  id: number;
  driverId: number;
  driver?: Driver;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DriverScheduleResponse extends DriverSchedule {
  driverId: number;
}

export interface BulkDriverScheduleRequest {
  schedules: DriverScheduleRequest[];
}

export interface DriverWithSchedules {
  driver: Driver;
  schedules: DriverScheduleResponse[];
}

export interface DriverScheduleStatistics {
  totalSchedules: number;
  activeSchedules: number;
  inactiveSchedules: number;
  schedulesByDay: Record<string, number>;
}

export interface DriverScheduleFilters {
  searchTerm: string;
  dayFilter: string;
  driverFilter: string;
  statusFilter: string;
}