import { useState } from 'react';
import type { 
  DriverScheduleResponse, 
  DriverScheduleRequest, 
  DriverWithSchedules 
} from '@/app/Dashboard/(driver)/driver-schedule/types/driverScheduleTypes';
import { driverScheduleService } from '../services/driverScheduleService';

export const useDriverScheduleActions = (
  onRefreshSchedules?: () => Promise<void>,
  setIsFormModalOpen?: (isOpen: boolean) => void,
  setEditingSchedules?: (schedules: DriverScheduleResponse[]) => void
) => {

  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [driverToDelete, setDriverToDelete] = useState<DriverWithSchedules | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const handleCreateSchedule = () => {
    if (setIsFormModalOpen && setEditingSchedules) {
      setIsFormModalOpen(true);
      setEditingSchedules([]);
    }
  };

  const handleEditSchedule = (driverWithSchedules: DriverWithSchedules) => {
    if (setIsFormModalOpen && setEditingSchedules) {
      setIsFormModalOpen(true);
      setEditingSchedules(driverWithSchedules.schedules);
    }
  };

  const openDeleteModal = (driverWithSchedules: DriverWithSchedules) => {
    setDriverToDelete(driverWithSchedules);
    setIsDeleteModalOpen(true);
  };

  const closeDeleteModal = () => {
    setIsDeleteModalOpen(false);
    setDriverToDelete(null);
  };

  const confirmDeleteSchedule = async () => {
    if (!driverToDelete) return;

    setIsDeleting(true);
    try {
      // Delete all schedules for this driver using the new endpoint
      await driverScheduleService.deleteDriverSchedulesByDriver(driverToDelete.driver.id);
      closeDeleteModal();
      if (onRefreshSchedules) {
        await onRefreshSchedules();
      }
    } catch (error: any) {
      console.error('Error deleting driver schedules:', error);
      alert(error.message || 'Error al eliminar los horarios del conductor');
    } finally {
      setIsDeleting(false);
    }
  };

  return {
    handleCreateSchedule,
    handleEditSchedule,
    openDeleteModal,
    closeDeleteModal,
    confirmDeleteSchedule,
    isDeleteModalOpen,
    driverToDelete,
    isDeleting,
  };
};