import { useState, useEffect, useMemo } from 'react';
import type { 
  DriverScheduleResponse, 
  DriverWithSchedules,
  DriverScheduleFilters,
  DriverScheduleStatistics
} from '../types/driverScheduleTypes';
import type { Driver } from '../../../drivers/types/driverTypes';
import { useDriversContext } from '../../../drivers/context/DriverContext';
import { driverScheduleService } from '../services/driverScheduleService';

export interface UseDriverSchedulesReturn {
  driverSchedules: DriverScheduleResponse[];
  driversWithSchedules: DriverWithSchedules[];
  filteredDriversWithSchedules: DriverWithSchedules[];
  paginatedDriversWithSchedules: DriverWithSchedules[];
  searchTerm: string;
  dayFilter: string;
  driverFilter: string;
  statusFilter: string;
  statistics: DriverScheduleStatistics;
  pagination: PaginationData;
  loading: boolean;
  error: string | null;
  setSearchTerm: (term: string) => void;
  setDayFilter: (filter: string) => void;
  setDriverFilter: (filter: string) => void;
  setStatusFilter: (filter: string) => void;
  setPage: (page: number) => void;
  setItemsPerPage: (itemsPerPage: number) => void;
  fetchDriverSchedules: () => Promise<void>;
}

export interface PaginationData {
  currentPage: number;
  totalPages: number;
  totalItems: number;
  itemsPerPage: number;
  startIndex: number;
  endIndex: number;
}

export const useDriverSchedules = (): UseDriverSchedulesReturn => {
  const { drivers } = useDriversContext();
  const [driverSchedules, setDriverSchedules] = useState<DriverScheduleResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [dayFilter, setDayFilter] = useState('all');
  const [driverFilter, setDriverFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(10);

  const fetchDriverSchedules = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await driverScheduleService.getAllDriverSchedules();
      // Normalize dayOfWeek to uppercase
      const normalizedData = data.map((schedule: DriverScheduleResponse) => ({
        ...schedule,
        dayOfWeek: schedule.dayOfWeek.toUpperCase()
      }));
      setDriverSchedules(normalizedData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error fetching driver schedules');
    } finally {
      setLoading(false);
    }
  };

  // Group schedules by driver
  const driversWithSchedules = useMemo(() => {
    const grouped = driverSchedules.reduce((acc, schedule) => {
      const driverId = schedule.driverId;
      if (!acc[driverId]) {
        const driver = drivers.find((d: Driver) => d.id === driverId);
        if (driver) {
          acc[driverId] = {
            driver,
            schedules: []
          };
        }
      }
      if (acc[driverId]) {
        acc[driverId].schedules.push(schedule);
      }
      return acc;
    }, {} as Record<number, DriverWithSchedules>);

    return Object.values(grouped);
  }, [driverSchedules, drivers]);

  // Filtered drivers with schedules
  const filteredDriversWithSchedules = useMemo(() => {
    return driversWithSchedules.filter((driverWithSchedules: DriverWithSchedules) => {
      const { driver, schedules } = driverWithSchedules;

      // Filter by driver name/identification
      const fullName = `${driver.firstName} ${driver.lastName}`.toLowerCase();
      const matchesDriverSearch = !searchTerm ||
        fullName.includes(searchTerm.toLowerCase()) ||
        driver.idNumber.toLowerCase().includes(searchTerm.toLowerCase());

      // Filter by driver selection
      const matchesDriverFilter = driverFilter === 'all' || driver.id!.toString() === driverFilter;

      // Filter by day - if a driver has at least one schedule matching the day filter
      const matchesDay = dayFilter === 'all' ||
        schedules.some(schedule => schedule.dayOfWeek === dayFilter);

      // Filter by status - if a driver has at least one schedule matching the status filter
      const matchesStatus = statusFilter === 'all' ||
        schedules.some((schedule: DriverScheduleResponse) =>
          (statusFilter === 'active' && schedule.active) ||
          (statusFilter === 'inactive' && !schedule.active)
        );

      return matchesDriverSearch && matchesDriverFilter && matchesDay && matchesStatus;
    });
  }, [driversWithSchedules, searchTerm, dayFilter, driverFilter, statusFilter]);

  // Statistics
  const statistics: DriverScheduleStatistics = useMemo(() => {
    const totalSchedules = driverSchedules.length;
    const activeSchedules = driverSchedules.filter(s => s.active).length;
    const inactiveSchedules = totalSchedules - activeSchedules;

      const schedulesByDay = driverSchedules.reduce((acc: Record<string, number>, schedule: DriverScheduleResponse) => {
      acc[schedule.dayOfWeek] = (acc[schedule.dayOfWeek] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);

    return {
      totalSchedules,
      activeSchedules,
      inactiveSchedules,
      schedulesByDay,
    };
  }, [driverSchedules]);

  // Pagination
  const pagination: PaginationData = useMemo(() => {
    const totalItems = filteredDriversWithSchedules.length;
    const totalPages = Math.ceil(totalItems / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = Math.min(startIndex + itemsPerPage, totalItems);

    return {
      currentPage,
      totalPages,
      totalItems,
      itemsPerPage,
      startIndex,
      endIndex,
    };
  }, [filteredDriversWithSchedules.length, currentPage, itemsPerPage]);

  const paginatedDriversWithSchedules = useMemo(() => {
    const { startIndex, endIndex } = pagination;
    return filteredDriversWithSchedules.slice(startIndex, endIndex);
  }, [filteredDriversWithSchedules, pagination]);

  // Reset to first page when filters change
  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, dayFilter, driverFilter, statusFilter]);

  useEffect(() => {
    fetchDriverSchedules();
  }, []);

  return {
    driverSchedules,
    driversWithSchedules,
    filteredDriversWithSchedules,
    paginatedDriversWithSchedules,
    searchTerm,
    dayFilter,
    driverFilter,
    statusFilter,
    statistics,
    pagination,
    loading,
    error,
    setSearchTerm,
    setDayFilter,
    setDriverFilter,
    setStatusFilter,
    setPage: setCurrentPage,
    setItemsPerPage,
    fetchDriverSchedules,
  };
};