import { Driver } from '../../../drivers/types/driverTypes';

export interface DriverSchedule {
  id: number;
  driverId: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DriverScheduleRequest {
  driverId: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
}

export interface DriverScheduleResponse extends Omit<DriverSchedule, 'driverId'> {
  driver: Driver;
}

export interface BulkDriverScheduleRequest {
  schedules: DriverScheduleRequest[];
}

export interface DriverWithSchedules extends Driver {
  schedules: DriverScheduleResponse[];
}

export interface DriverScheduleFilters {
  searchTerm: string;
  dayFilter: string;
  driverFilter: string;
  statusFilter: string;
}