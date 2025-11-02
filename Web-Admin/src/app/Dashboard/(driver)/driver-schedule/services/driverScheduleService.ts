import { DriverScheduleApi } from './api/driverScheduleApi';
import type { DriverScheduleRequest, DriverScheduleResponse, BulkDriverScheduleRequest } from '../types/driverScheduleTypes';

export const driverScheduleService = {
  async getAllDriverSchedules(): Promise<DriverScheduleResponse[]> {
    const response = await DriverScheduleApi.getAll();
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.message || 'Error fetching driver schedules');
  },

  async getDriverScheduleById(id: number): Promise<DriverScheduleResponse> {
    const response = await DriverScheduleApi.getById(id);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.message || 'Error fetching driver schedule');
  },

  async createDriverSchedule(data: DriverScheduleRequest): Promise<DriverScheduleResponse> {
    const response = await DriverScheduleApi.create(data);
    console.log('Create Response:', response); // Debug log
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.message || 'Error creating driver schedule');
  },

  async updateDriverSchedule(id: number, data: DriverScheduleRequest): Promise<DriverScheduleResponse> {
    const response = await DriverScheduleApi.update(id, data);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.message || 'Error updating driver schedule');
  },

  async deleteDriverSchedule(id: number): Promise<void> {
    const response = await DriverScheduleApi.delete(id);
    if (!response.success) {
      throw new Error(response.message || 'Error deleting driver schedule');
    }
  },

  async getDriverSchedulesByDriver(driverId: number): Promise<DriverScheduleResponse[]> {
    const response = await DriverScheduleApi.getByDriver(driverId);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.message || 'Error fetching driver schedules by driver');
  },

  async createDriverSchedules(data: BulkDriverScheduleRequest): Promise<DriverScheduleResponse[]> {
    const response = await DriverScheduleApi.createBulk(data);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.message || 'Error creating driver schedules');
  },

  async updateDriverSchedules(driverId: number, data: BulkDriverScheduleRequest): Promise<DriverScheduleResponse[]> {
    try {
      console.log('Updating schedules for driver:', driverId);
      console.log('New schedules data:', data);

      const response = await DriverScheduleApi.updateBulk(driverId, data);
      if (!response.success) {
        throw new Error(response.message || 'Error updating schedules');
      }
      
      if (!response.data) {
        throw new Error('No data received from update bulk operation');
      }
      
      return response.data;
    } catch (error: any) {
      console.error('Error in updateDriverSchedules:', error);
      throw new Error(error.message || 'Error updating driver schedules');
    }
  },

  async deleteDriverSchedulesByDriver(driverId: number): Promise<void> {
    const response = await DriverScheduleApi.deleteByDriver(driverId);
    if (!response.success) {
      throw new Error(response.message || 'Error deleting driver schedules by driver');
    }
  },
};