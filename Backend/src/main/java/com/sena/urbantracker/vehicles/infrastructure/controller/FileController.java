package com.sena.urbantracker.vehicles.infrastructure.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controlador para servir archivos estáticos (imágenes)
 */
@Slf4j
@RestController
@RequestMapping("/uploads")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8085"})
public class FileController {

    @Value("${app.upload.directory:./uploads}")
    private String uploadDirectory;

    /**
     * Servir imágenes de vehículos
     */
    @GetMapping("/vehicles/{filename:.+}")
    public ResponseEntity<Resource> serveVehicleImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDirectory, "vehicles", filename);
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("Image not found or not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // Determinar el tipo de contenido basado en la extensión
            String contentType = determineContentType(filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving image: {}", filename, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Servir cualquier archivo en el directorio de uploads
     */
    @GetMapping("/{subdirectory}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String subdirectory, @PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDirectory, subdirectory, filename);
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("File not found or not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            String contentType = determineContentType(filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving file: {}/{}", subdirectory, filename, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Determinar el tipo de contenido basado en la extensión del archivo
     */
    private String determineContentType(String filename) {
        String extension = filename.toLowerCase();
        
        if (extension.endsWith(".png")) {
            return "image/png";
        } else if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (extension.endsWith(".gif")) {
            return "image/gif";
        } else if (extension.endsWith(".webp")) {
            return "image/webp";
        } else if (extension.endsWith(".bmp")) {
            return "image/bmp";
        } else if (extension.endsWith(".svg")) {
            return "image/svg+xml";
        } else {
            return "application/octet-stream";
        }
    }
}
