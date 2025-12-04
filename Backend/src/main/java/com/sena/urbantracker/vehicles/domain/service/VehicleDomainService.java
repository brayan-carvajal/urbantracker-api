package com.sena.urbantracker.vehicles.domain.service;

import com.sena.urbantracker.shared.application.dto.CrudResponseDto;
import com.sena.urbantracker.shared.infrastructure.exception.EntityNotFoundException;
import com.sena.urbantracker.users.domain.entity.CompanyDomain;
import com.sena.urbantracker.users.domain.repository.CompanyRepository;
import com.sena.urbantracker.vehicles.application.dto.request.VehicleReqDto;
import com.sena.urbantracker.vehicles.application.dto.response.VehicleResDto;
import com.sena.urbantracker.vehicles.application.mapper.VehicleMapper;
import com.sena.urbantracker.vehicles.domain.entity.VehicleDomain;
import com.sena.urbantracker.vehicles.domain.entity.VehicleTypeDomain;
import com.sena.urbantracker.vehicles.domain.repository.VehicleRepository;
import com.sena.urbantracker.vehicles.domain.repository.VehicleTypeRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para operaciones de vehículos con archivos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleDomainService {

    private final VehicleRepository vehicleRepository;
    private final CompanyRepository companyRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final HttpServletRequest request;

    @Value("${app.upload.directory:./uploads}")
    private String uploadDirectory;

    @Value("${app.upload.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    /**
     * Guardar imagen y retornar los datos binarios y tipo de contenido
     */
    public ImageData saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        }

        // Validar tamaño (5MB máximo)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("El archivo no puede ser mayor a 5MB");
        }

        try {
            // Leer los bytes del archivo
            byte[] imageData = file.getBytes();

            return new ImageData(imageData, contentType);

        } catch (IOException e) {
            log.error("Error leyendo imagen: {}", file.getOriginalFilename(), e);
            throw new IOException("Error al procesar la imagen", e);
        }
    }

    /**
     * Clase interna para retornar datos de imagen
     */
    public static class ImageData {
        private final byte[] data;
        private final String contentType;

        public ImageData(byte[] data, String contentType) {
            this.data = data;
            this.contentType = contentType;
        }

        public byte[] getData() { return data; }
        public String getContentType() { return contentType; }
    }

    /**
     * Crear vehículo con archivos (imágenes se pasan como MultipartFile)
     */
    public CrudResponseDto<VehicleResDto> createWithFiles(VehicleReqDto vehicleReqDto, MultipartFile outboundImage, MultipartFile returnImage) {
        log.info("Creating vehicle with files: {}", vehicleReqDto.getLicencePlate());

        try {
            // Convertir DTO a dominio
            VehicleDomain vehicleDomain = VehicleMapper.toEntity(vehicleReqDto);

            // Asignar relaciones si están presentes
            if (vehicleReqDto.getCompanyId() != null) {
                CompanyDomain company = companyRepository.findById(vehicleReqDto.getCompanyId())
                        .orElseThrow(() -> new EntityNotFoundException("Compañía no encontrada con ID: " + vehicleReqDto.getCompanyId()));
                vehicleDomain.setCompany(company);
            }
            if (vehicleReqDto.getVehicleTypeId() != null) {
                VehicleTypeDomain vehicleType = vehicleTypeRepository.findById(vehicleReqDto.getVehicleTypeId())
                        .orElseThrow(() -> new EntityNotFoundException("Tipo de vehículo no encontrado con ID: " + vehicleReqDto.getVehicleTypeId()));
                vehicleDomain.setVehicleType(vehicleType);
            }

            // Validar que no exista un vehículo con la misma placa
            if (vehicleRepository.existsByLicencePlate(vehicleDomain.getLicencePlate())) {
                throw new IllegalArgumentException("Ya existe un vehículo con esta placa");
            }

            // Procesar imágenes si están presentes
            if (outboundImage != null && !outboundImage.isEmpty()) {
                ImageData imageData = saveImage(outboundImage);
                vehicleDomain.setOutboundImageData(imageData.getData());
                vehicleDomain.setOutboundImageContentType(imageData.getContentType());
            }


            // Guardar vehículo
            VehicleDomain savedVehicle = vehicleRepository.save(vehicleDomain);

            // Convertir a DTO de respuesta
            VehicleResDto responseDto = VehicleMapper.toDto(savedVehicle);

            log.info("Vehicle created successfully with ID: {}", savedVehicle.getId());
            return CrudResponseDto.success(responseDto, "Vehículo creado exitosamente");

        } catch (Exception e) {
            log.error("Error creating vehicle with files", e);
            return CrudResponseDto.error("Error al crear el vehículo: " + e.getMessage());
        }
    }

    /**
     * Actualizar vehículo con archivos (imágenes se pasan como MultipartFile)
     */
    public CrudResponseDto<VehicleResDto> updateWithFiles(Long id, VehicleReqDto vehicleReqDto, MultipartFile outboundImage, MultipartFile returnImage) {
        log.info("Updating vehicle {} with files", id);

        try {
            // Buscar vehículo existente
            VehicleDomain existingVehicle = vehicleRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));

            // Verificar que la placa no esté en uso por otro vehículo
            if (!existingVehicle.getLicencePlate().equals(vehicleReqDto.getLicencePlate()) &&
                vehicleRepository.existsByLicencePlate(vehicleReqDto.getLicencePlate())) {
                throw new IllegalArgumentException("Ya existe un vehículo con esta placa");
            }

            // Actualizar datos básicos (solo si no son null)
            if (vehicleReqDto.getBrand() != null && !vehicleReqDto.getBrand().trim().isEmpty()) {
                existingVehicle.setBrand(vehicleReqDto.getBrand());
            }
            if (vehicleReqDto.getModel() != null && !vehicleReqDto.getModel().trim().isEmpty()) {
                existingVehicle.setModel(vehicleReqDto.getModel());
            }
            if (vehicleReqDto.getYear() != null) {
                existingVehicle.setYear(vehicleReqDto.getYear());
            }
            if (vehicleReqDto.getColor() != null) {
                existingVehicle.setColor(vehicleReqDto.getColor());
            }
            if (vehicleReqDto.getPassengerCapacity() != null) {
                existingVehicle.setPassengerCapacity(vehicleReqDto.getPassengerCapacity());
            }
            if (vehicleReqDto.getStatus() != null) {
                existingVehicle.setStatus(vehicleReqDto.getStatus());
            }
            // Para inService, siempre actualizar ya que puede ser true/false
            existingVehicle.setInService(vehicleReqDto.isInService());
            // Para licencePlate, siempre actualizar ya que es identificador
            if (vehicleReqDto.getLicencePlate() != null && !vehicleReqDto.getLicencePlate().trim().isEmpty()) {
                existingVehicle.setLicencePlate(vehicleReqDto.getLicencePlate());
            }
            // Para companyId y vehicleTypeId, actualizar si están presentes
            if (vehicleReqDto.getCompanyId() != null) {
                CompanyDomain company = companyRepository.findById(vehicleReqDto.getCompanyId())
                        .orElseThrow(() -> new EntityNotFoundException("Compañía no encontrada con ID: " + vehicleReqDto.getCompanyId()));
                existingVehicle.setCompany(company);
            }
            if (vehicleReqDto.getVehicleTypeId() != null) {
                VehicleTypeDomain vehicleType = vehicleTypeRepository.findById(vehicleReqDto.getVehicleTypeId())
                        .orElseThrow(() -> new EntityNotFoundException("Tipo de vehículo no encontrado con ID: " + vehicleReqDto.getVehicleTypeId()));
                existingVehicle.setVehicleType(vehicleType);
            }

            // Procesar imágenes
            if (outboundImage != null && !outboundImage.isEmpty()) {
                // Subir nueva imagen
                ImageData imageData = saveImage(outboundImage);
                existingVehicle.setOutboundImageData(imageData.getData());
                existingVehicle.setOutboundImageContentType(imageData.getContentType());
            } else {
                // No se envió nueva imagen, eliminar la existente si había
                if (existingVehicle.getOutboundImageData() != null) {
                    existingVehicle.setOutboundImageData(null);
                    existingVehicle.setOutboundImageContentType(null);
                }
            }


            // Guardar vehículo actualizado
            VehicleDomain savedVehicle = vehicleRepository.save(existingVehicle);

            // Convertir a DTO de respuesta
            VehicleResDto responseDto = VehicleMapper.toDto(savedVehicle);

            return CrudResponseDto.success(responseDto, "Vehículo actualizado exitosamente");

        } catch (Exception e) {
            log.error("Error updating vehicle with files", e);
            return CrudResponseDto.error("Error al actualizar el vehículo: " + e.getMessage());
        }
    }


    /**
     * Extraer extensión del archivo
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return ".jpg"; // extensión por defecto
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    /**
     * Extraer nombre del archivo desde la URL
     */
    private String extractFilenameFromUrl(String imageUrl) {
        if (imageUrl == null) {
            return null;
        }
        
        String[] parts = imageUrl.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }

    /**
     * Obtener todos los vehículos
     */
    public List<VehicleResDto> findAll() {
        log.info("Finding all vehicles");
        
        try {
            List<VehicleDomain> vehicles = vehicleRepository.findAll();
            
            return vehicles.stream()
                .map(VehicleMapper::toDto)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error finding all vehicles", e);
            throw new RuntimeException("Error al obtener todos los vehículos", e);
        }
    }

    /**
     * Obtener vehículo por ID
     */
    public Optional<VehicleResDto> findById(Long id) {
        log.info("Finding vehicle by ID: {}", id);

        try {
            Optional<VehicleDomain> vehicleOpt = vehicleRepository.findByIdWithRelations(id);

            return vehicleOpt.map(VehicleMapper::toDto);

        } catch (Exception e) {
            log.error("Error finding vehicle by ID: {}", id, e);
            throw new RuntimeException("Error al obtener el vehículo por ID", e);
        }
    }

    /**
     * Eliminar vehículo por ID
     */
    public void delete(Long id) {
        log.info("Deleting vehicle by ID: {}", id);

        try {
            // Verificar que el vehículo existe
            VehicleDomain vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));

            // Eliminar vehículo de la base de datos (las imágenes se eliminan automáticamente con CASCADE)
            vehicleRepository.deleteById(id);

            log.info("Vehicle deleted successfully: {}", id);

        } catch (Exception e) {
            log.error("Error deleting vehicle by ID: {}", id, e);
            throw new RuntimeException("Error al eliminar el vehículo", e);
        }
    }

    /**
     * Determinar la URL base para imágenes basándose en el entorno
     */
    private String determineImageBaseUrl() {
        try {
            // Detectar si estamos ejecutándonos en Docker
            boolean isDocker = checkIfRunningInDocker();
            
            if (isDocker) {
                log.debug("Detected Docker environment, using service URL");
                return "http://backend:8080";
            }
            
            // Para desarrollo, siempre usar localhost con el puerto del servidor
            return "http://localhost:" + serverPort;
            
        } catch (Exception e) {
            log.warn("Error determining image base URL, using fallback: {}", baseUrl, e);
            return baseUrl;
        }
    }

    /**
     * Detectar si estamos ejecutándose en Docker
     */
    private boolean checkIfRunningInDocker() {
        // Verificar variables de entorno específicas de Docker
        String dockerEnv = System.getenv("DOCKER_ENV");
        if ("true".equals(dockerEnv)) {
            return true;
        }
        
        // Verificar si existe el archivo .dockerenv (típico de Docker)
        if (new java.io.File("/.dockerenv").exists()) {
            return true;
        }
        
        // Verificar cgroup para detectar Docker
        try {
            String cgroup = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("/proc/1/cgroup")));
            if (cgroup.contains("docker") || cgroup.contains("kubepods")) {
                return true;
            }
        } catch (Exception e) {
            // Ignorar errores al leer cgroup
        }
        
        return false;
    }

    /**
     * Validar que las URLs de imágenes sean accesibles
     */
    private void validateImageUrls(String outboundImageUrl, String returnImageUrl) {
        // Validar imagen outbound si existe
        if (StringUtils.hasText(outboundImageUrl)) {
            validateImageAccessibility(outboundImageUrl, "outbound");
        }
        
        // Validar imagen return si existe
        if (StringUtils.hasText(returnImageUrl)) {
            validateImageAccessibility(returnImageUrl, "return");
        }
    }

    /**
     * Validar que una imagen específica sea accesible
     */
    private void validateImageAccessibility(String imageUrl, String imageType) {
        try {
            // Extraer el nombre del archivo de la URL
            String filename = extractFilenameFromUrl(imageUrl);
            if (filename == null) {
                throw new IllegalArgumentException("URL de imagen inválida para " + imageType + ": " + imageUrl);
            }
            
            // Construir la ruta local del archivo
            Path imagePath = Paths.get(uploadDirectory, "vehicles", filename);
            
            // Verificar que el archivo existe y es legible
            if (!Files.exists(imagePath)) {
                throw new IllegalArgumentException("Imagen " + imageType + " no encontrada en el servidor: " + filename);
            }
            
            if (!Files.isReadable(imagePath)) {
                throw new IllegalArgumentException("Imagen " + imageType + " no es legible: " + filename);
            }
            
            // Verificar que la URL es accesible (por ahora solo log, en producción se podría hacer un HTTP request)
            log.debug("Imagen {} validada correctamente: {}", imageType, imageUrl);
            
        } catch (Exception e) {
            log.warn("Error validando imagen {} ({}): {}", imageType, imageUrl, e.getMessage());
            // No lanzar excepción aquí, solo log, ya que el archivo podría estar siendo procesado aún
        }
    }
}
