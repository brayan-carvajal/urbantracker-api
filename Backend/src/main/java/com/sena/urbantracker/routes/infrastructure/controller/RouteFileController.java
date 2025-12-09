package com.sena.urbantracker.routes.infrastructure.controller;

import com.sena.urbantracker.shared.application.dto.CrudResponseDto;
import com.sena.urbantracker.routes.application.dto.request.RouteReqDto;
import com.sena.urbantracker.routes.application.dto.response.RouteResDto;
import com.sena.urbantracker.routes.application.dto.response.RouteDetailsResDto;
import com.sena.urbantracker.routes.application.service.RouteService;
import com.sena.urbantracker.routes.infrastructure.persistence.model.RouteModel;
import com.sena.urbantracker.routes.infrastructure.repository.jpa.RouteJpaRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Optional;

/**
 * Controlador específico para manejar operaciones de rutas con archivos
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/routes")
// @PreAuthorize("hasRole('ADMIN')") // Temporalmente comentado para testing
public class RouteFileController {

    private final RouteService routeService;
    private final RouteJpaRepository routeJpaRepository;

    public RouteFileController(RouteService routeService, RouteJpaRepository routeJpaRepository) {
        this.routeService = routeService;
        this.routeJpaRepository = routeJpaRepository;
    }

    /**
     * Crear ruta con archivos
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<CrudResponseDto<RouteResDto>> createWithFiles(
            @Valid @ModelAttribute RouteReqDto routeReqDto,
            @RequestParam(value = "outboundImage", required = false) MultipartFile outboundImage,
            @RequestParam(value = "returnImage", required = false) MultipartFile returnImage) {

        log.info("Creating route with files: {}", routeReqDto.getNumberRoute());

        // Validar archivos antes de procesar
        try {
            validateImageFile(outboundImage, "imagen de ida");
            validateImageFile(returnImage, "imagen de vuelta");
        } catch (IllegalArgumentException validationError) {
            log.warn("Image validation failed: {}", validationError.getMessage());
            CrudResponseDto<RouteResDto> errorResponse = CrudResponseDto.error(validationError.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Las imágenes se procesarán directamente en el servicio

        // Crear la ruta usando el servicio
        try {
            CrudResponseDto<RouteResDto> response = routeService.create(routeReqDto);

            if (response.success) {
                log.info("Route created successfully with ID: {}", response.getData().getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                log.error("Route creation failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("Error creating route with files", e);
            CrudResponseDto<RouteResDto> errorResponse = CrudResponseDto.error(
                "Error interno del servidor al crear la ruta");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Actualizar ruta con archivos
     */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<CrudResponseDto<RouteResDto>> updateWithFiles(
            @PathVariable Long id,
            @Valid @ModelAttribute RouteReqDto routeReqDto,
            @RequestParam(value = "outboundImage", required = false) MultipartFile outboundImage,
            @RequestParam(value = "returnImage", required = false) MultipartFile returnImage) {

        log.info("Updating route {} with files", id);

        try {
            // Las imágenes se procesarán directamente en el servicio

            // Actualizar la ruta usando el servicio
            CrudResponseDto<RouteResDto> response = routeService.update(routeReqDto, id);

            if (response.success) {
                log.info("Route updated successfully: {}", response.getData().getId());
                return ResponseEntity.ok(response);
            } else {
                log.error("Route update failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (IllegalArgumentException e) {
            log.error("Validation error updating route {}: {}", id, e.getMessage());
            CrudResponseDto<RouteResDto> errorResponse = CrudResponseDto.error(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error updating route with files", e);
            CrudResponseDto<RouteResDto> errorResponse = CrudResponseDto.error("Error interno del servidor al actualizar la ruta");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Eliminar imagen de ruta
     */
    @DeleteMapping("/{id}/images/{imageType}")
    public ResponseEntity<CrudResponseDto<Void>> deleteImage(
            @PathVariable Long id,
            @PathVariable String imageType) {

        log.info("Deleting {} image for route {}", imageType, id);

        try {
            routeService.deleteImage(id, imageType);

            CrudResponseDto<Void> response = CrudResponseDto.success(
                null,
                "Imagen eliminada exitosamente"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting image", e);
            CrudResponseDto<Void> errorResponse = CrudResponseDto.error("Error al eliminar la imagen");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtener todas las rutas
     */
    @GetMapping
    public ResponseEntity<CrudResponseDto<List<RouteResDto>>> getAllRoutes() {
        log.info("Getting all routes");

        try {
            CrudResponseDto<List<RouteResDto>> routes = routeService.findAll();

            return ResponseEntity.ok(routes);

        } catch (Exception e) {
            log.error("Error getting all routes", e);
            return ResponseEntity.internalServerError()
                .body(CrudResponseDto.error("Error al obtener las rutas"));
        }
    }

    /**
     * Obtener ruta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CrudResponseDto<RouteResDto>> getRouteById(@PathVariable Long id) {
        log.info("Getting route by ID: {}", id);

        try {
            CrudResponseDto<Optional<RouteResDto>> routeOpt = routeService.findById(id);

            if (routeOpt.success && routeOpt.getData().isPresent()) {
                CrudResponseDto<RouteResDto> response = CrudResponseDto.success(
                    routeOpt.getData().get(),
                    "Ruta obtenida exitosamente"
                );
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error getting route by ID: {}", id, e);
            return ResponseEntity.internalServerError()
                .body(CrudResponseDto.error("Error al obtener la ruta"));
        }
    }

    /**
     * Obtener detalles de ruta por tipo (WAYPOINT, GEOMETRY)
     */
    @GetMapping("/{id}/{type}")
    public ResponseEntity<CrudResponseDto<RouteDetailsResDto>> getRouteDetails(
            @PathVariable Long id,
            @PathVariable String type) {
        log.info("Getting route details for ID: {} and type: {}", id, type);

        try {
            CrudResponseDto<RouteDetailsResDto> response =
                routeService.findByIdType(id, type);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting route details for ID: {} and type: {}", id, type, e);
            return ResponseEntity.internalServerError()
                .body(CrudResponseDto.error("Error al obtener los detalles de la ruta"));
        }
    }

    /**
     * Eliminar ruta
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<CrudResponseDto<Void>> deleteRoute(@PathVariable Long id) {
        log.info("Deleting route: {}", id);

        try {
            CrudResponseDto<RouteResDto> response = routeService.deleteById(id);

            if (response.success) {
                CrudResponseDto<Void> successResponse = CrudResponseDto.success(
                    null,
                    "Ruta eliminada exitosamente"
                );
                return ResponseEntity.ok(successResponse);
            } else {
                return ResponseEntity.badRequest().body(CrudResponseDto.error(response.getMessage()));
            }

        } catch (Exception e) {
            log.error("Error deleting route", e);
            return ResponseEntity.internalServerError()
                .body(CrudResponseDto.error("Error al eliminar la ruta"));
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
     * Obtener extensión del archivo
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }

    /**
     * Servir imágenes de rutas desde la base de datos
     */
    @GetMapping("/{id}/images/{imageType}")
    // @PreAuthorize("hasRole('ADMIN')") // Imágenes públicas para visualización
    public ResponseEntity<byte[]> getRouteImage(@PathVariable Long id, @PathVariable String imageType) {
        log.info("Serving {} image for route {}", imageType, id);

        try {
            Optional<RouteModel> routeOpt = routeJpaRepository.findById(id);

            if (routeOpt.isEmpty()) {
                log.warn("Route with ID {} not found", id);
                return ResponseEntity.notFound().build();
            }

            RouteModel route = routeOpt.get();
            log.debug("Route found: {}", route.getNumberRoute());

            byte[] imageData = null;
            String contentType = null;

            // Determinar qué imagen servir
            if ("outbound".equals(imageType)) {
                imageData = route.getOutboundImageData();
                contentType = route.getOutboundImageContentType();
                log.debug("Outbound image data length: {}", imageData != null ? imageData.length : "null");
            } else if ("return".equals(imageType)) {
                imageData = route.getReturnImageData();
                contentType = route.getReturnImageContentType();
                log.debug("Return image data length: {}", imageData != null ? imageData.length : "null");
            } else {
                log.warn("Invalid image type: {} (only 'outbound' or 'return' supported for routes)", imageType);
                return ResponseEntity.badRequest().build();
            }

            if (imageData == null || imageData.length == 0) {
                log.warn("No image data found for route {} type {}", id, imageType);
                return ResponseEntity.notFound().build();
            }

            // Usar content type por defecto si no está especificado
            if (contentType == null || contentType.isEmpty()) {
                contentType = "image/jpeg";
                log.debug("Using default content type: {}", contentType);
            }

            log.info("Serving image for route {} type {} ({} bytes, {})", id, imageType, imageData.length, contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageData);

        } catch (Exception e) {
            log.error("Error serving image for route {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}