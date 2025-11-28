-- Crea todos los schemas que se necesitan
CREATE SCHEMA IF NOT EXISTS monitoring;
CREATE SCHEMA IF NOT EXISTS parking;
CREATE SCHEMA IF NOT EXISTS reports;
CREATE SCHEMA IF NOT EXISTS routes;
CREATE SCHEMA IF NOT EXISTS security;
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS vehicles;

-- Tabla de waypoints de rutas
CREATE TABLE IF NOT EXISTS routes.route_waypoint (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    sequence INTEGER NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    type VARCHAR(50) NOT NULL,
    destine VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_route_waypoint_route FOREIGN KEY (route_id) REFERENCES routes.route(id) ON DELETE CASCADE,
    CONSTRAINT uk_route_waypoint UNIQUE (route_id, sequence, type, destine)
);

-- Índices para route_waypoint
CREATE INDEX IF NOT EXISTS idx_route_waypoint_route_id ON routes.route_waypoint(route_id);
CREATE INDEX IF NOT EXISTS idx_route_waypoint_type ON routes.route_waypoint(type);

-- Tabla de rutas
CREATE TABLE IF NOT EXISTS routes.route (
    id BIGSERIAL PRIMARY KEY,
    number_route INTEGER NOT NULL UNIQUE,
    description TEXT,
    total_distance DOUBLE PRECISION,
    outbound_image_url TEXT,
    return_image_url TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para route
CREATE INDEX IF NOT EXISTS idx_route_number_route ON routes.route(number_route);
CREATE INDEX IF NOT EXISTS idx_route_active ON routes.route(active);

-- Tabla de configuración de estacionamiento
CREATE TABLE IF NOT EXISTS parking.parking_config (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    min_time_minutes INTEGER NOT NULL CHECK (min_time_minutes >= 1 AND min_time_minutes <= 480),
    max_distance_meters DOUBLE PRECISION NOT NULL CHECK (max_distance_meters >= 1.0 AND max_distance_meters <= 1000.0),
    max_speed_kmh DOUBLE PRECISION NOT NULL CHECK (max_speed_kmh >= 0.1 AND max_speed_kmh <= 50.0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para parking_config
CREATE INDEX IF NOT EXISTS idx_parking_config_company_id ON parking.parking_config(company_id);
CREATE INDEX IF NOT EXISTS idx_parking_config_active ON parking.parking_config(is_active) WHERE is_active = true;

-- Tabla de eventos de estacionamiento
CREATE TABLE IF NOT EXISTS parking.parking_event (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id VARCHAR(50) NOT NULL,
    driver_id BIGINT,
    route_id BIGINT,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    total_duration_minutes INTEGER,
    final_location_lat DECIMAL(10,8),
    final_location_lng DECIMAL(11,8),
    is_active BOOLEAN NOT NULL DEFAULT false,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para parking_event
CREATE INDEX IF NOT EXISTS idx_parking_event_vehicle_id ON parking.parking_event(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_parking_event_driver_id ON parking.parking_event(driver_id);
CREATE INDEX IF NOT EXISTS idx_parking_event_route_id ON parking.parking_event(route_id);
CREATE INDEX IF NOT EXISTS idx_parking_event_started_at ON parking.parking_event(started_at);
CREATE INDEX IF NOT EXISTS idx_parking_event_is_active ON parking.parking_event(is_active) WHERE is_active = true;

-- Comentarios en las tablas
COMMENT ON TABLE parking.parking_config IS 'Configuración de parámetros para detección automática de estacionamiento por compañía';
COMMENT ON TABLE parking.parking_event IS 'Registro de eventos de estacionamiento detectados automáticamente';

-- Comentarios en columnas
COMMENT ON COLUMN parking.parking_config.min_time_minutes IS 'Tiempo mínimo en minutos para considerar que un vehículo está estacionado';
COMMENT ON COLUMN parking.parking_config.max_distance_meters IS 'Distancia máxima en metros que puede recorrer un vehículo estacionado';
COMMENT ON COLUMN parking.parking_config.max_speed_kmh IS 'Velocidad máxima en km/h para considerar que un vehículo está estacionado';
COMMENT ON COLUMN parking.parking_config.is_active IS 'Indica si la configuración está activa para detección';

COMMENT ON COLUMN parking.parking_event.vehicle_id IS 'ID del vehículo que se estacionó';
COMMENT ON COLUMN parking.parking_event.driver_id IS 'ID del conductor (opcional)';
COMMENT ON COLUMN parking.parking_event.route_id IS 'ID de la ruta asignada (opcional)';
COMMENT ON COLUMN parking.parking_event.started_at IS 'Fecha y hora cuando comenzó el estacionamiento';
COMMENT ON COLUMN parking.parking_event.ended_at IS 'Fecha y hora cuando terminó el estacionamiento';
COMMENT ON COLUMN parking.parking_event.total_duration_minutes IS 'Duración total del estacionamiento en minutos';
COMMENT ON COLUMN parking.parking_event.final_location_lat IS 'Latitud final del vehículo estacionado';
COMMENT ON COLUMN parking.parking_event.final_location_lng IS 'Longitud final del vehículo estacionado';
COMMENT ON COLUMN parking.parking_event.is_active IS 'Indica si el evento de estacionamiento está actualmente activo';