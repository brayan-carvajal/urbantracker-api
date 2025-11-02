"use client";

import React, { useEffect, useState } from 'react';
import { Loader2 } from 'lucide-react';
import { useDriverSchedules } from '@/app/Dashboard/(driver)/driver-schedule/hooks/useDriverSchedules';
import { useDriverScheduleActions } from '@/app/Dashboard/(driver)/driver-schedule/hooks/useDriverScheduleActions';
import { DriverScheduleHeader } from '@/app/Dashboard/(driver)/driver-schedule/components/DriverScheduleHeader';
import { DriverScheduleCard } from '@/app/Dashboard/(driver)/driver-schedule/components/DriverScheduleCard';
import { DriverScheduleFilters } from '@/app/Dashboard/(driver)/driver-schedule/components/DriverScheduleFilters';
import { StatisticsCards } from '@/app/Dashboard/(driver)/driver-schedule/components/StatisticsCards';
import { DeleteConfirmationModal } from './components/DeleteConfirmationModal';
import DriverScheduleFormManager from './components/DriverScheduleFormManager';
import { Sheet, SheetContent, SheetTitle } from "@/components/ui/sheet";
import { Pagination } from '@/app/Dashboard/drivers/components/Pagination';
import type {
  DriverScheduleResponse,
  DriverScheduleRequest,
  BulkDriverScheduleRequest,
  DriverWithSchedules
} from '@/app/Dashboard/(driver)/driver-schedule/types/driverScheduleTypes';
import type { Driver } from '@/app/Dashboard/drivers/types/driverTypes';
import { driverScheduleService } from './services/driverScheduleService';

export default function DriverSchedulePage() {
  // Modal states
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [editingSchedules, setEditingSchedules] = useState<DriverScheduleResponse[]>([]);
  const [isFormLoading, setIsFormLoading] = useState(false);

  const {
    driversWithSchedules,
    filteredDriversWithSchedules,
    paginatedDriversWithSchedules,
    pagination,
    searchTerm,
    dayFilter,
    driverFilter,
    statusFilter,
    statistics,
    loading,
    error,
    setSearchTerm,
    setDayFilter,
    setDriverFilter,
    setStatusFilter,
    setPage,
    setItemsPerPage,
    fetchDriverSchedules
  } = useDriverSchedules();

  const {
    openDeleteModal,
    closeDeleteModal,
    confirmDeleteSchedule,
    isDeleteModalOpen,
    driverToDelete,
    isDeleting,
    handleCreateSchedule,
    handleEditSchedule
  } = useDriverScheduleActions(
    fetchDriverSchedules,
    setIsFormModalOpen,
    setEditingSchedules
  );



  const handleCloseFormModal = () => {
    setIsFormModalOpen(false);
    setEditingSchedules([]);
  };

  const handleSaveSchedule = async (data: BulkDriverScheduleRequest) => {
    setIsFormLoading(true);
    try {
      console.log('Starting to save schedules...', JSON.stringify(data, null, 2));
      console.log('Editing schedules:', JSON.stringify(editingSchedules, null, 2));

      if (editingSchedules.length > 0) {
        // Para edición: eliminar horarios existentes del conductor y crear los nuevos
        const driverId = editingSchedules[0]?.driverId;
        if (!driverId) {
          throw new Error('No se pudo encontrar el ID del conductor');
        }
        console.log('Updating schedules for driver:', driverId);
        
        // Validar la estructura de los datos antes de enviar
        if (!data.schedules || !Array.isArray(data.schedules) || data.schedules.length === 0) {
          throw new Error('Los datos del horario no son válidos');
        }

        await driverScheduleService.updateDriverSchedules(driverId, {
          schedules: data.schedules.map(schedule => ({
            ...schedule,
            driverId: driverId
          }))
        });
      } else {
        console.log('Creating new schedules:', JSON.stringify(data, null, 2));
        
        // Validar la estructura de los datos antes de enviar
        if (!data.schedules || !Array.isArray(data.schedules) || data.schedules.length === 0) {
          throw new Error('Los datos del horario no son válidos');
        }

        await driverScheduleService.createDriverSchedules(data);
      }
      
      console.log('Successfully saved schedules, refreshing data...');
      await fetchDriverSchedules();
      console.log('Data refreshed successfully');
      handleCloseFormModal();
    } catch (error: any) {
      console.error('Error saving schedules:', {
        error,
        message: error.message,
        stack: error.stack,
        data: JSON.stringify(data, null, 2)
      });
      // Mostrar el error al usuario en lugar de lanzarlo
      alert(`Error al guardar los horarios: ${error.message || 'Error desconocido'}`);
    } finally {
      setIsFormLoading(false);
    }
  };

  useEffect(() => {
    fetchDriverSchedules();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-black p-6">
        <div className="flex items-center justify-center h-64">
          <div className="flex items-center gap-3 text-zinc-300">
            <Loader2 className="h-8 w-8 animate-spin text-emerald-500" />
            <span className="text-lg">Cargando Horarios...</span>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-black p-6">
        <div className="text-center py-12">
          <div className="text-zinc-400 text-lg">
            Error al cargar los horarios: {error}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8 bg-black min-h-screen p-6">
      {/* Header */}
      <DriverScheduleHeader onCreateSchedule={handleCreateSchedule} />

      {/* Statistics */}
      <StatisticsCards statistics={statistics} />

      {/* Filters */}
      <DriverScheduleFilters
        searchTerm={searchTerm}
        dayFilter={dayFilter}
        driverFilter={driverFilter}
        statusFilter={statusFilter}
        onSearchChange={setSearchTerm}
        onDayFilterChange={setDayFilter}
        onDriverFilterChange={setDriverFilter}
        onStatusFilterChange={setStatusFilter}
      />

      {/* Schedule list */}
      <section className="space-y-6">
        {paginatedDriversWithSchedules.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-zinc-400 text-lg">
              {searchTerm || dayFilter !== "all" || driverFilter !== "all" || statusFilter !== "all"
                ? "No se encontraron horarios con los filtros aplicados"
                : "No hay horarios registrados"}
            </div>
            {!searchTerm && dayFilter === "all" && driverFilter === "all" && statusFilter === "all" && (
              <button
                onClick={handleCreateSchedule}
                className="mt-4 bg-emerald-600 hover:bg-emerald-700 text-white px-6 py-3 rounded-lg transition-colors"
              >
                Crear primer horario
              </button>
            )}
          </div>
        ) : (
          <>
            <div className="grid gap-6">
              {paginatedDriversWithSchedules.map((driverWithSchedules) => (
                <DriverScheduleCard
                  key={driverWithSchedules.driver.id}
                  driverWithSchedules={driverWithSchedules}
                  onEdit={handleEditSchedule}
                  onDelete={openDeleteModal}
                />
              ))}
            </div>
            {/* Pagination */}
            <Pagination
              pagination={{
                currentPage: pagination.currentPage,
                totalPages: pagination.totalPages,
                totalItems: pagination.totalItems,
                itemsPerPage: pagination.itemsPerPage,
                startIndex: pagination.startIndex,
                endIndex: pagination.endIndex
              }}
              onPageChange={setPage}
              onItemsPerPageChange={setItemsPerPage}
              isLoading={loading}
            />
          </>
        )}
      </section>

      {/* Form Modal */}
      <Sheet open={isFormModalOpen} onOpenChange={handleCloseFormModal}>
        <SheetContent side="right" className="bg-zinc-900 text-white w-full sm:max-w-2xl p-4">
          <div className="flex flex-col gap-4">
            <SheetTitle className="text-lg font-semibold text-white">
              {editingSchedules.length > 0 ? 'Editar Horario' : 'Crear Horario'}
            </SheetTitle>
            <DriverScheduleFormManager
              onSave={handleSaveSchedule}
              editingSchedules={editingSchedules}
              mode={editingSchedules.length > 0 ? 'edit' : 'create'}
            />
          </div>
        </SheetContent>
      </Sheet>

      {/* Delete Confirmation Modal */}
      <DeleteConfirmationModal
        isOpen={isDeleteModalOpen}
        onClose={closeDeleteModal}
        onConfirm={confirmDeleteSchedule}
        driver={driverToDelete}
        isDeleting={isDeleting}
      />
    </div>
  );
}
