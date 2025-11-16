# urbantracker-api# UrbanTracker-api ⚙️

**UrbanTracker API** es el backend de la plataforma de monitoreo de transporte público en tiempo real. Esta API proporciona todos los servicios necesarios para gestionar usuarios, vehículos, rutas y datos de localización en tiempo real.

> Asegúrate de tener **Java 17** y **Maven** instalados para ejecutar el proyecto localmente.

---

## 🏗️ Arquitectura

El API sigue una arquitectura basada en **Domain Driven Design (DDD)** con una separación clara entre capas:

- **`Security`**: Autenticación, autorización y gestión de usuarios
- **`Users`**: Gestión de conductores, empresas y perfiles de usuario  
- **`Vehicles`**: Administración de vehículos, tipos y asignaciones
- **`Routes`**: Gestión de rutas y trayectorias
- **`Shared`**: Componentes comunes y utilidades

### 🛠️ Tecnologías

![Java](https://img.shields.io/badge/java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/spring%20boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/postgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20Web%20Tokens&logoColor=white)
![MQTT](https://img.shields.io/badge/MQTT-660066?style=for-the-badge&logo=MQTT&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)

> Framework Spring Boot 3.3.4 con Java 17. Base de datos PostgreSQL con comunicación en tiempo real vía MQTT y WebSockets.

---

## 🗄️ Base de Datos

### PostgreSQL
La aplicación utiliza PostgreSQL como base de datos principal para almacenar toda la información del sistema.

### MQTT Broker (Mosquitto)
Incluye un broker MQTT integrado para manejar la comunicación en tiempo real con los dispositivos GPS y otras comunicaciones del sistema.

---

## ⚡ Características Principales

### 🔐 Sistema de Seguridad
- Autenticación JWT
- Gestión de roles y permisos
- Recuperación de contraseña por email
- Validación OTP

### 👥 Gestión de Usuarios
- Conductores y administradores
- Empresas de transporte
- Perfiles de usuario personalizables
- Horarios de conductores

### 🚌 Administración de Vehículos
- Registro de vehículos
- Tipos de vehículos
- Asignación vehículo-conductor
- Seguimiento de estado en tiempo real

### 🗺️ Gestión de Rutas
- Definición de rutas y trayectorias
- Puntos de parada (waypoints)
- Horarios de rutas

### 📡 Comunicación en Tiempo Real
- WebSockets para actualizaciones live
- MQTT para dispositivos IoT
- Streaming de datos GPS

---

## 🚀 Instalación y Ejecución

### Prerrequisitos
- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Docker (opcional para servicios adicionales)

### 📥 Clonar y Configurar

```bash
# 1. Navega al directorio del backend
cd Backend

# 2. Configura las variables de entorno
# Copia y ajusta el archivo de configuración
cp src/main/resources/application.properties.example src/main/resources/application.properties

# 3. Configura la base de datos PostgreSQL
# Asegúrate de que PostgreSQL esté corriendo y las credenciales sean correctas
```

### 🛠️ Construcción y Ejecución

```bash
# 1. Instala las dependencias
mvn clean install

# 2. Ejecuta las migraciones de base de datos
mvn flyway:migrate

# 3. Ejecuta la aplicación
mvn spring-boot:run
```

### 🐳 Ejecutar con Docker

```bash
# Inicia todos los servicios (API + MQTT Broker + Base de datos)
docker-compose up -d

# O ejecuta solo el broker MQTT si ya tienes el backend corriendo
docker-compose up mosquitto
```

---

## 📚 Endpoints Principales

### Autenticación
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/forgot-password` - Solicitar recuperación
- `POST /api/auth/verify-otp` - Verificar código OTP

### Usuarios
- `GET /api/users` - Listar usuarios
- `POST /api/users` - Crear usuario
- `PUT /api/users/{id}` - Actualizar usuario
- `DELETE /api/users/{id}` - Eliminar usuario

### Conductores
- `GET /api/drivers` - Listar conductores
- `POST /api/drivers` - Crear conductor
- `PUT /api/drivers/{id}` - Actualizar conductor
- `GET /api/drivers/{id}/schedules` - Obtener horarios

### Vehículos
- `GET /api/vehicles` - Listar vehículos
- `POST /api/vehicles` - Crear vehículo
- `PUT /api/vehicles/{id}` - Actualizar vehículo
- `GET /api/vehicles/{id}/assignments` - Ver asignaciones

### Rutas
- `GET /api/routes` - Listar rutas
- `POST /api/routes` - Crear ruta
- `PUT /api/routes/{id}` - Actualizar ruta
- `GET /api/routes/{id}/trajectory` - Obtener trayectoria

---

## 🔌 WebSocket Endpoints

### Ubicaciones en Tiempo Real
- `ws://localhost:8080/ws/locations` - Stream de ubicaciones de vehículos

### MQTT Topics
- `urbantracker/gps/{vehicleId}` - Datos GPS de vehículos
- `urbantracker/status/{vehicleId}` - Estado de vehículos

---

## 📖 Documentación API

Una vez ejecutando la aplicación, accede a la documentación interactiva en:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

---

## 🧪 Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests específicos
mvn test -Dtest=UserServiceTest

# Generar reporte de cobertura
mvn jacoco:report
```

---

## 🔧 Configuración

### Variables de Entorno Principales

```env
# Base de datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=urbantracker
DB_USERNAME=postgres
DB_PASSWORD=password

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email
MAIL_PASSWORD=your-password

# MQTT
MQTT_BROKER_URL=tcp://localhost:1883
MQTT_CLIENT_ID=urbantracker-api
```

---

## 📦 Estructura del Proyecto

```
Backend/
├── src/main/java/com/sena/urbantracker/
│   ├── security/           # Autenticación y autorización
│   │   ├── application/    # Lógica de negocio
│   │   ├── domain/         # Entidades de dominio
│   │   ├── infrastructure/ # Implementación y persistencia
│   │   └── ... controllers, services, repositories
│   ├── users/              # Gestión de usuarios
│   ├── vehicles/           # Administración de vehículos
│   ├── routes/             # Gestión de rutas
│   └── shared/             # Componentes compartidos
├── src/main/resources/
│   ├── application.properties
│   └── schema-init.sql
└── pom.xml
```

---

📌 *Este API proporciona toda la funcionalidad backend para las aplicaciones Web-Client y Web-Admin de UrbanTracker. Asegúrate de que esté ejecutándose correctamente antes de iniciar las aplicaciones frontend.*