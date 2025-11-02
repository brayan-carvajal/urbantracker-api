import { ApiClient } from '../../../../utils/apiClient';
import type { DriverScheduleRequest, DriverScheduleResponse, BulkDriverScheduleRequest } from '../../types/driverScheduleTypes';
import type { CrudResponse } from './types';
import { API_ENDPOINTS } from './config';

const apiClient = new ApiClient("http://localhost:8080");

export class DriverScheduleApi {
  static async getAll(): Promise<CrudResponse<DriverScheduleResponse[]>> {
    return apiClient.get<CrudResponse<DriverScheduleResponse[]>>(API_ENDPOINTS.DRIVER_SCHEDULES);
  }

  static async getById(id: number): Promise<CrudResponse<DriverScheduleResponse>> {
    return apiClient.get<CrudResponse<DriverScheduleResponse>>(`${API_ENDPOINTS.DRIVER_SCHEDULES}/${id}`);
  }

  static async create(data: DriverScheduleRequest): Promise<CrudResponse<DriverScheduleResponse>> {
    return apiClient.post<CrudResponse<DriverScheduleResponse>>(API_ENDPOINTS.DRIVER_SCHEDULES, data);
  }

  static async update(id: number, data: DriverScheduleRequest): Promise<CrudResponse<DriverScheduleResponse>> {
    return apiClient.put<CrudResponse<DriverScheduleResponse>>(`${API_ENDPOINTS.DRIVER_SCHEDULES}/${id}`, data);
  }

  static async delete(id: number): Promise<CrudResponse<void>> {
    return apiClient.delete<CrudResponse<void>>(`${API_ENDPOINTS.DRIVER_SCHEDULES}/${id}`);
  }

  static async getByDriver(driverId: number): Promise<CrudResponse<DriverScheduleResponse[]>> {
    return apiClient.get<CrudResponse<DriverScheduleResponse[]>>(`${API_ENDPOINTS.DRIVER_SCHEDULES}/driver/${driverId}`);
  }

  static async createBulk(data: BulkDriverScheduleRequest): Promise<CrudResponse<DriverScheduleResponse[]>> {
    return apiClient.post<CrudResponse<DriverScheduleResponse[]>>(`${API_ENDPOINTS.DRIVER_SCHEDULES}/bulk`, data.schedules);
  }

  static async updateBulk(driverId: number, data: BulkDriverScheduleRequest): Promise<CrudResponse<DriverScheduleResponse[]>> {
    return apiClient.put<CrudResponse<DriverScheduleResponse[]>>(`${API_ENDPOINTS.DRIVER_SCHEDULES}/bulk/${driverId}`, data.schedules);
  }

  static async deleteByDriver(driverId: number): Promise<CrudResponse<void>> {
    try {
      const response = await apiClient.delete<CrudResponse<void>>(`${API_ENDPOINTS.DRIVER_SCHEDULES}/driver/${driverId}`);
      if (!response.success) {
        throw new Error(response.message || 'Error al eliminar los horarios del conductor');
      }
      return response;
    } catch (error: any) {
      console.error('API Error:', error);
      throw new Error(error.message || 'Error al comunicarse con el servidor');
    }
  }
}