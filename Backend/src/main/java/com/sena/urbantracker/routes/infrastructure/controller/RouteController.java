package com.sena.urbantracker.routes.infrastructure.controller;

import com.sena.urbantracker.routes.application.dto.request.RouteReqDto;
import com.sena.urbantracker.routes.application.dto.response.RouteDetailsResDto;
import com.sena.urbantracker.routes.application.dto.response.RouteResDto;
import com.sena.urbantracker.routes.application.service.RouteService;
import com.sena.urbantracker.shared.application.dto.CrudResponseDto;
import com.sena.urbantracker.shared.infrastructure.controller.BaseController;
import com.sena.urbantracker.shared.domain.enums.EntityType;
import com.sena.urbantracker.shared.application.service.ServiceFactory;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/route")
public class RouteController extends BaseController<RouteReqDto, RouteResDto, Long> {

    private final RouteService routeService;

    public RouteController(ServiceFactory serviceFactory, RouteService routeService) {
        super(serviceFactory, EntityType.ROUTE, RouteReqDto.class, RouteResDto.class);
        this.routeService = routeService;
    }

    @PostMapping("/with-images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CrudResponseDto<RouteResDto>> create(@ModelAttribute RouteReqDto dto) throws BadRequestException {
        return super.create(dto);
    }

    @Override
    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CrudResponseDto<RouteResDto>> update(@PathVariable Long id, @ModelAttribute RouteReqDto dto) throws BadRequestException {
        return super.update(id, dto);
    }


    @GetMapping("/{id}/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CrudResponseDto<RouteDetailsResDto>> viewEdit(@PathVariable Long id, @PathVariable String type) throws BadRequestException {
        return ResponseEntity.ok(routeService.findByIdType(id, type));
    }

    /**
     * Migrar URLs de imágenes para rutas existentes
     */
    @PostMapping("/migrate-image-urls")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CrudResponseDto<String>> migrateImageUrls() {
        try {
            routeService.updateExistingImageUrls();
            return ResponseEntity.ok(CrudResponseDto.success("URLs de imágenes migradas exitosamente", "Migración completada"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(CrudResponseDto.error("Error durante la migración: " + e.getMessage()));
        }
    }

    /**
     * Endpoint público para migrar URLs (temporal, quitar en producción)
     */
    @GetMapping("/migrate-image-urls-public")
    public ResponseEntity<String> migrateImageUrlsPublic() {
        try {
            routeService.updateExistingImageUrls();
            return ResponseEntity.ok("URLs de imágenes migradas exitosamente");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error durante la migración: " + e.getMessage());
        }
    }

    /**
     * Servir imágenes de rutas (para compatibilidad con URLs existentes)
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

