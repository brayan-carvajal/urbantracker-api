package com.sena.urbantracker.vehicles.infrastructure.controller;

import com.sena.urbantracker.shared.application.dto.CrudResponseDto;
import com.sena.urbantracker.shared.application.service.ServiceFactory;
import com.sena.urbantracker.vehicles.application.dto.request.VehicleReqDto;
import com.sena.urbantracker.vehicles.application.dto.response.VehicleResDto;
import com.sena.urbantracker.vehicles.domain.entity.VehicleDomain;
import com.sena.urbantracker.vehicles.domain.repository.VehicleRepository;
import com.sena.urbantracker.vehicles.domain.service.VehicleDomainService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controlador específico para manejar operaciones de vehículos con archivos
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/vehicles")
// @PreAuthorize("hasRole('ADMIN')") // Temporalmente comentado para testing
public class VehicleFileController {

    private final VehicleDomainService vehicleDomainService;
    private final VehicleRepository vehicleRepository;
    private final ServiceFactory serviceFactory;

    public VehicleFileController(VehicleDomainService vehicleDomainService, VehicleRepository vehicleRepository, ServiceFactory serviceFactory) {
        this.vehicleDomainService = vehicleDomainService;
        this.vehicleRepository = vehicleRepository;
        this.serviceFactory = serviceFactory;
    }

    /**
     * Crear vehículo con archivos
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<CrudResponseDto<VehicleResDto>> createWithFiles(
            @Valid @ModelAttribute VehicleReqDto vehicleReqDto,
            @RequestParam(value = "outboundImage", required = false) MultipartFile outboundImage,
            @RequestParam(value = "returnImage", required = false) MultipartFile returnImage) {
        
        log.info("Creating vehicle with files: {}", vehicleReqDto.getLicencePlate());
        
        // Validar archivos antes de procesar
        try {
            validateImageFile(outboundImage, "imagen de salida");
            validateImageFile(returnImage, "imagen de retorno");
        } catch (IllegalArgumentException validationError) {
            log.warn("Image validation failed: {}", validationError.getMessage());
            CrudResponseDto<VehicleResDto> errorResponse = CrudResponseDto.error(validationError.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Crear el vehículo usando el servicio con archivos
        try {
            CrudResponseDto<VehicleResDto> response = vehicleDomainService.createWithFiles(vehicleReqDto, outboundImage, returnImage);
            
            if (response.success) {
                log.info("Vehicle created successfully with ID: {}", response.getData().getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                log.error("Vehicle creation failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error creating vehicle with files", e);
            CrudResponseDto<VehicleResDto> errorResponse = CrudResponseDto.error(
                "Error interno del servidor al crear el vehículo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Actualizar vehículo con archivos
     */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @Transactional
    public ResponseEntity<CrudResponseDto<VehicleResDto>> updateWithFiles(
            @PathVariable Long id,
            @Valid @ModelAttribute VehicleReqDto vehicleReqDto,
            @RequestParam(value = "outboundImage", required = false) MultipartFile outboundImage,
            @RequestParam(value = "returnImage", required = false) MultipartFile returnImage) throws Exception {

        log.info("Updating vehicle {} with files", id);

        try {
            // Actualizar el vehículo usando el servicio con archivos
            CrudResponseDto<VehicleResDto> response = vehicleDomainService.updateWithFiles(id, vehicleReqDto, outboundImage, returnImage);
            
            if (response.success) {
                log.info("Vehicle updated successfully: {}", response.getData().getId());
                return ResponseEntity.ok(response);
            } else {
                log.error("Vehicle update failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error updating vehicle {}: {}", id, e.getMessage());
            CrudResponseDto<VehicleResDto> errorResponse = CrudResponseDto.error(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error updating vehicle with files", e);
            CrudResponseDto<VehicleResDto> errorResponse = CrudResponseDto.error("Error interno del servidor al actualizar el vehículo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    /**
     * Obtener todos los vehículos
     */
    @GetMapping
    public ResponseEntity<CrudResponseDto<List<VehicleResDto>>> getAllVehicles() {
        log.info("Getting all vehicles");
        
        try {
            List<VehicleResDto> vehicles = vehicleDomainService.findAll();
            
            CrudResponseDto<List<VehicleResDto>> response = CrudResponseDto.success(
                vehicles, 
                "Vehículos obtenidos exitosamente"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting all vehicles", e);
            return ResponseEntity.internalServerError()
                .body(CrudResponseDto.error("Error al obtener los vehículos"));
        }
    }

    /**
     * Obtener vehículo por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CrudResponseDto<VehicleResDto>> getVehicleById(@PathVariable Long id) {
        log.info("Getting vehicle by ID: {}", id);
        
        try {
            Optional<VehicleResDto> vehicleOpt = vehicleDomainService.findById(id);
            
            if (vehicleOpt.isPresent()) {
                CrudResponseDto<VehicleResDto> response = CrudResponseDto.success(
                    vehicleOpt.get(), 
                    "Vehículo obtenido exitosamente"
                );
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error getting vehicle by ID: {}", id, e);
            return ResponseEntity.internalServerError()
                .body(CrudResponseDto.error("Error al obtener el vehículo"));
        }
    }

    /**
     * Eliminar vehículo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<CrudResponseDto<Void>> deleteVehicle(@PathVariable Long id) {
        log.info("Deleting vehicle: {}", id);
        
        try {
            vehicleDomainService.delete(id);
            
            CrudResponseDto<Void> response = CrudResponseDto.success(
                null, 
                "Vehículo eliminado exitosamente"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting vehicle", e);
            return ResponseEntity.internalServerError()
                .body(CrudResponseDto.error("Error al eliminar el vehículo"));
        }
    }

    /**
     * Validar archivo de imagen antes del procesamiento
     */
    private void validateImageFile(MultipartFile file, String fileDescription) {
        if (file == null || file.isEmpty()) {
            return; // Los archivos son opcionales
        }
        
        // Validar tipo de contenido
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo " + fileDescription + " debe ser una imagen válida");
        }
        
        // Validar tamaño (5MB máximo)
        long maxSizeBytes = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                "El archivo " + fileDescription + " no puede ser mayor a 5MB (actual: " + 
                (file.getSize() / (1024 * 1024)) + "MB)");
        }
        
        // Validar nombre de archivo
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("El archivo " + fileDescription + " debe tener un nombre válido");
        }
        
        // Validar extensión
        String extension = getFileExtension(originalFilename);
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("El archivo " + fileDescription + " debe tener una extensión válida");
        }
        
        log.debug("File {} validated successfully: {} ({} bytes, {})", 
            fileDescription, originalFilename, file.getSize(), contentType);
    }

    /**
     * Servir imágenes de vehículos desde la base de datos
     */
    @GetMapping("/{id}/images/{imageType}")
    // @PreAuthorize("hasRole('ADMIN')") // Imágenes públicas para visualización
    public ResponseEntity<byte[]> getVehicleImage(@PathVariable Long id, @PathVariable String imageType) {
        log.info("Serving {} image for vehicle {}", imageType, id);

        try {
            Optional<VehicleDomain> vehicleOpt = vehicleRepository.findById(id);

            if (vehicleOpt.isEmpty()) {
                log.warn("Vehicle with ID {} not found", id);
                return ResponseEntity.notFound().build();
            }

            VehicleDomain vehicle = vehicleOpt.get();
            log.debug("Vehicle found: {}", vehicle.getLicencePlate());

            byte[] imageData = null;
            String contentType = null;

            // Determinar qué imagen servir
            if ("outbound".equals(imageType)) {
                imageData = vehicle.getOutboundImageData();
                contentType = vehicle.getOutboundImageContentType();
                log.debug("Outbound image data length: {}", imageData != null ? imageData.length : "null");
            } else {
                log.warn("Invalid image type: {} (only 'outbound' supported for vehicles)", imageType);
                return ResponseEntity.badRequest().build();
            }

            if (imageData == null || imageData.length == 0) {
                log.warn("No image data found for vehicle {} type {}", id, imageType);
                return ResponseEntity.notFound().build();
            }

            // Usar content type por defecto si no está especificado
            if (contentType == null || contentType.isEmpty()) {
                contentType = "image/jpeg";
                log.debug("Using default content type: {}", contentType);
            }

            log.info("Serving image for vehicle {} type {} ({} bytes, {})", id, imageType, imageData.length, contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageData);

        } catch (Exception e) {
            log.error("Error serving image for vehicle {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener extensión del archivo
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }
}
