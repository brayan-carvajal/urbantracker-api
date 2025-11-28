package com.sena.urbantracker.routes.infrastructure.controller;

import com.sena.urbantracker.shared.application.dto.CrudResponseDto;
import com.sena.urbantracker.routes.application.dto.request.RouteReqDto;
import com.sena.urbantracker.routes.application.dto.response.RouteResDto;
import com.sena.urbantracker.routes.application.dto.response.RouteDetailsResDto;
import com.sena.urbantracker.routes.application.service.RouteService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public RouteFileController(RouteService routeService) {
        this.routeService = routeService;
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

        // Procesar archivos y generar URLs
        String outboundImageUrl = null;
        String returnImageUrl = null;

        try {
            if (outboundImage != null && !outboundImage.isEmpty()) {
                log.debug("Processing outbound image: {} ({} bytes)", outboundImage.getOriginalFilename(), outboundImage.getSize());
                outboundImageUrl = routeService.saveImage(outboundImage, Integer.valueOf(routeReqDto.getNumberRoute()), "outbound");
                log.debug("Outbound image saved successfully: {}", outboundImageUrl);
            }

            if (returnImage != null && !returnImage.isEmpty()) {
                log.debug("Processing return image: {} ({} bytes)", returnImage.getOriginalFilename(), returnImage.getSize());
                returnImageUrl = routeService.saveImage(returnImage, Integer.valueOf(routeReqDto.getNumberRoute()), "return");
                log.debug("Return image saved successfully: {}", returnImageUrl);
            }

        } catch (Exception e) {
            log.error("Error processing image files", e);
            CrudResponseDto<RouteResDto> errorResponse = CrudResponseDto.error(
                "Error al procesar las imágenes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        // Establecer las URLs en el DTO
        routeReqDto.setOutboundImageUrl(outboundImageUrl);
        routeReqDto.setReturnImageUrl(returnImageUrl);

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
            @RequestParam(value = "returnImage", required = false) MultipartFile returnImage) throws Exception {

        log.info("Updating route {} with files", id);

        try {
            // Procesar archivos y generar URLs
            String outboundImageUrl = null;
            String returnImageUrl = null;

            if (outboundImage != null && !outboundImage.isEmpty()) {
                outboundImageUrl = routeService.saveImage(outboundImage, Integer.valueOf(routeReqDto.getNumberRoute()), "outbound");
            }

            if (returnImage != null && !returnImage.isEmpty()) {
                returnImageUrl = routeService.saveImage(returnImage, Integer.valueOf(routeReqDto.getNumberRoute()), "return");
            }

            // Establecer las URLs en el DTO
            routeReqDto.setOutboundImageUrl(outboundImageUrl);
            routeReqDto.setReturnImageUrl(returnImageUrl);

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
            @PathVariable String imageType) throws Exception {

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
            throw e;
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
            CrudResponseDto<com.sena.urbantracker.routes.application.dto.response.RouteDetailsResDto> response =
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
     * Servir imágenes de rutas (nueva ruta plural)
     */
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveRouteImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("src/main/resources/static/images/routes", filename);
            Resource resource = new FileSystemResource(filePath);

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}