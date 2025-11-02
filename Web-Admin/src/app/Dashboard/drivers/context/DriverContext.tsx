"use client";

import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Driver } from '../types/driverTypes';
import { driverService } from '../services/driverService';

interface DriverContextType {
  drivers: Driver[];
  loading: boolean;
  error: string | null;
  refreshDrivers: () => Promise<void>;
}

const DriverContext = createContext<DriverContextType | undefined>(undefined);

export const DriverProvider = ({ children }: { children: ReactNode }) => {
  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refreshDrivers = async () => {
    try {
      setLoading(true);
      const fetchedDrivers = await driverService.getAll();
      setDrivers(fetchedDrivers);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error fetching drivers');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshDrivers();
  }, []);

  return (
    <DriverContext.Provider value={{ drivers, loading, error, refreshDrivers }}>
      {children}
    </DriverContext.Provider>
  );
};

export const useDriversContext = () => {
  const context = useContext(DriverContext);
  if (context === undefined) {
    throw new Error('useDriversContext must be used within a DriverProvider');
  }
  return context;
};