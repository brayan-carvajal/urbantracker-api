package com.sena.urbantracker.routes.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sena.urbantracker.monitoring.application.service.mqtt.DynamicSubscriptionService;
import com.sena.urbantracker.routes.application.dto.request.RouteReqDto;
import com.sena.urbantracker.routes.application.dto.request.RouteWaypointReqDto;
import com.sena.urbantracker.routes.application.dto.response.RouteDetailsResDto;
import com.sena.urbantracker.routes.application.dto.response.RouteResDto;
import com.sena.urbantracker.routes.application.dto.response.RouteWaypointResDto;
import com.sena.urbantracker.routes.application.mapper.RouteMapper;
import com.sena.urbantracker.routes.application.mapper.RouteWaypointMapper;
import com.sena.urbantracker.routes.domain.entity.RouteDomain;
import com.sena.urbantracker.routes.domain.entity.RouteWaypointDomain;
import com.sena.urbantracker.routes.domain.repository.RouteRepository;
import com.sena.urbantracker.routes.domain.repository.RouteWaypointRepository;
import com.sena.urbantracker.shared.infrastructure.exception.EntityAlreadyExistsException;
import com.sena.urbantracker.shared.infrastructure.exception.EntityNotFoundException;
import com.sena.urbantracker.shared.application.dto.CrudResponseDto;
import com.sena.urbantracker.shared.domain.repository.CrudOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService implements CrudOperations<RouteReqDto, RouteResDto, Long> {

    private final RouteRepository routeRepository;
    private final RouteWaypointRepository routeWaypointRepository;
    private final ObjectMapper objectMapper;
    private final DynamicSubscriptionService dynamicSubscriptionService;

    @Value("${app.upload.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Transactional(rollbackFor = BadRequestException.class)
    @Override
    public CrudResponseDto<RouteResDto> create(RouteReqDto request) throws BadRequestException {
        Integer numberRouteInt = Integer.valueOf(request.getNumberRoute());
        if (routeRepository.existsByNumberRoute(numberRouteInt)) {
            throw new EntityAlreadyExistsException("Ya existe una ruta con número: " + request.getNumberRoute());
        }

        // Parseo temprano para evitar guardar la ruta si el JSON es inválido
        List<RouteWaypointReqDto> waypointDtos = parseJson(request.getWaypoints());

        RouteDomain route = RouteMapper.toEntity(request);

        // Manejar imágenes outbound
        if (request.getOutboundImage() != null && !request.getOutboundImage().isEmpty()) {
            try {
                byte[] outboundImageData = request.getOutboundImage().getBytes();
                String outboundContentType = request.getOutboundImage().getContentType();
                route.setOutboundImageData(outboundImageData);
                route.setOutboundImageContentType(outboundContentType);
            } catch (IOException e) {
                throw new RuntimeException("Error al procesar imagen de ida: " + e.getMessage());
            }
        }

        // Manejar imágenes return
        if (request.getReturnImage() != null && !request.getReturnImage().isEmpty()) {
            try {
                byte[] returnImageData = request.getReturnImage().getBytes();
                String returnContentType = request.getReturnImage().getContentType();
                route.setReturnImageData(returnImageData);
                route.setReturnImageContentType(returnContentType);
            } catch (IOException e) {
                throw new RuntimeException("Error al procesar imagen de vuelta: " + e.getMessage());
            }
        }

        // 1) Guarda la ruta y fuerza el INSERT si necesitas el ID ya mismo
        RouteDomain savedRoute = routeRepository.saveAndFlush(route); // <-- aquí "esperas" efectivamente

        // 2) Generar URLs de imágenes ahora que tenemos el ID
        boolean needsUpdate = false;
        if (savedRoute.getOutboundImageData() != null) {
            savedRoute.setOutboundImageUrl(generateImageUrl(savedRoute.getId(), "outbound"));
            needsUpdate = true;
        }
        if (savedRoute.getReturnImageData() != null) {
            savedRoute.setReturnImageUrl(generateImageUrl(savedRoute.getId(), "return"));
            needsUpdate = true;
        }

        // 3) Guardar nuevamente con las URLs actualizadas si es necesario
        final RouteDomain finalSavedRoute = needsUpdate ? routeRepository.saveAndFlush(savedRoute) : savedRoute;

        // 4) Mapea y guarda los waypoints con la FK a la ruta ya persistida
        List<RouteWaypointDomain> waypoints = waypointDtos.stream()
                .map(dto -> {
                    RouteWaypointDomain e = RouteWaypointMapper.toEntity(dto, finalSavedRoute.getId());
                    e.setRoute(finalSavedRoute);
                    return e;
                })
                .toList();

        routeWaypointRepository.saveAll(waypoints);

        // 5) crear el topic de la ruta usando el ID en lugar del número
        String routeTopic = "route/" + finalSavedRoute.getId();

        // 6) Suscribirse al topic de la ruta
        dynamicSubscriptionService.subscribeToRouteTopic(routeTopic);

        return CrudResponseDto.success(RouteMapper.toDto(finalSavedRoute, waypoints.size()), "Ruta creada correctamente y suscrito al topic: " + routeTopic);
    }


    @Override
    public CrudResponseDto<Optional<RouteResDto>> findById(Long id) {
        RouteDomain route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ruta con id " + id + " no encontrada."));


        return CrudResponseDto.success(Optional.of(RouteMapper.toDto(route, 0)), "Ruta encontrada");
    }

    @Override
    public CrudResponseDto<List<RouteResDto>> findAll() {
        log.info("[RouteService] findAll called");
        List<RouteDomain> routes = routeRepository.findAll();
        log.info("[RouteService] Found {} routes in database", routes.size());
        List<RouteResDto> routesDto = new ArrayList<>();

        for (RouteDomain route : routes) {
            boolean needsUpdate = false;

            // Generar URLs para imágenes existentes si no las tienen
            if (route.getOutboundImageData() != null && (route.getOutboundImageUrl() == null || route.getOutboundImageUrl().isEmpty())) {
                route.setOutboundImageUrl(generateImageUrl(route.getId(), "outbound"));
                needsUpdate = true;
            }
            if (route.getReturnImageData() != null && (route.getReturnImageUrl() == null || route.getReturnImageUrl().isEmpty())) {
                route.setReturnImageUrl(generateImageUrl(route.getId(), "return"));
                needsUpdate = true;
            }

            // Solo guardar si necesita actualización
            if (needsUpdate) {
                log.info("[RouteService] Updating route {} with image URLs", route.getId());
                routeRepository.save(route);
            }

            Integer numberWaypoints = routeWaypointRepository.countByTypeAndRoute("WAYPOINT", route);
            routesDto.add(RouteMapper.toDto(route, numberWaypoints));
        }

        log.info("[RouteService] Returning {} routes to frontend", routesDto.size());
        return CrudResponseDto.success(routesDto, "Listado de rutas");
    }


    @Transactional(rollbackFor = BadRequestException.class)
    @Override
    public CrudResponseDto<RouteResDto> update(RouteReqDto request, Long id) throws BadRequestException {
        RouteDomain route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se puede actualizar. Ruta no encontrada."));

        route.setNumberRoute(Integer.valueOf(request.getNumberRoute()));
        route.setDescription(request.getDescription());
        route.setTotalDistance(Double.valueOf(request.getTotalDistance()));

        // Manejo condicional de imágenes
        Integer numberRouteInt = Integer.valueOf(request.getNumberRoute());

        // Verificar si se debe eliminar imagen outbound
        if ("true".equals(request.getDeleteOutboundImage())) {
            route.setOutboundImageData(null);
            route.setOutboundImageContentType(null);
            route.setOutboundImageUrl(null);
        }
        // Outbound image - solo procesar si no se va a eliminar
        else if (request.getOutboundImage() != null && !request.getOutboundImage().isEmpty()) {
            try {
                byte[] outboundImageData = request.getOutboundImage().getBytes();
                String outboundContentType = request.getOutboundImage().getContentType();
                route.setOutboundImageData(outboundImageData);
                route.setOutboundImageContentType(outboundContentType);
                route.setOutboundImageUrl(generateImageUrl(route.getId(), "outbound"));
            } catch (IOException e) {
                throw new RuntimeException("Error al procesar imagen de ida: " + e.getMessage());
            }
        }
        // Si no se envía imagen nueva y no se elimina, mantener la existente

        // Verificar si se debe eliminar imagen return
        if ("true".equals(request.getDeleteReturnImage())) {
            route.setReturnImageData(null);
            route.setReturnImageContentType(null);
            route.setReturnImageUrl(null);
        }
        // Return image - solo procesar si no se va a eliminar
        else if (request.getReturnImage() != null && !request.getReturnImage().isEmpty()) {
            try {
                byte[] returnImageData = request.getReturnImage().getBytes();
                String returnContentType = request.getReturnImage().getContentType();
                route.setReturnImageData(returnImageData);
                route.setReturnImageContentType(returnContentType);
                route.setReturnImageUrl(generateImageUrl(route.getId(), "return"));
            } catch (IOException e) {
                throw new RuntimeException("Error al procesar imagen de vuelta: " + e.getMessage());
            }
        }
        // Si no se envía imagen nueva y no se elimina, mantener la existente

        RouteDomain updated = routeRepository.saveAndFlush(route);

        routeWaypointRepository.deleteByRoute(updated);

        List<RouteWaypointReqDto> waypointDtos = parseJson(request.getWaypoints());

        List<RouteWaypointDomain> waypoints = waypointDtos.stream()
                .map(dto -> {
                    RouteWaypointDomain e = RouteWaypointMapper.toEntity(dto, updated.getId());
                    e.setRoute(updated);
                    return e;
                })
                .toList();

        routeWaypointRepository.saveAll(waypoints);

        return CrudResponseDto.success(RouteMapper.toDto(updated, waypoints.size()), "Ruta actualizada correctamente");
    }

    @Override
    public CrudResponseDto<RouteResDto> deleteById(Long id) {
        RouteDomain route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ruta no encontrada."));

        // Las imágenes se eliminan automáticamente al eliminar la entidad (cascade)
        routeRepository.deleteById(id);
        return CrudResponseDto.success(RouteMapper.toDto(null, 0), "Ruta eliminada correctamente");
    }

    @Override
    public CrudResponseDto<RouteResDto> activateById(Long id) {
        RouteDomain route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ruta no encontrada."));
        route.setActive(true);
        routeRepository.save(route);
        return CrudResponseDto.success(RouteMapper.toDto(route, 0), "Ruta activada");
    }

    @Override
    public CrudResponseDto<RouteResDto> deactivateById(Long id) {
        RouteDomain route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ruta no encontrada."));
        route.setActive(false);
        routeRepository.save(route);
        return CrudResponseDto.success(RouteMapper.toDto(route, 0), "Ruta desactivada");
    }

    @Override
    public CrudResponseDto<Boolean> existsById(Long id) {
        return CrudResponseDto.success(routeRepository.existsById(id), "Verificación de existencia completada");
    }

    /**
     * Guardar imagen y retornar la URL (método público para controladores)
     */
    public String saveImage(MultipartFile file, Integer routeNumber, String type) {
        return saveImageInternal(file, routeNumber, type);
    }

    /**
     * Eliminar imagen de ruta (método público para controladores)
     */
    public void deleteImage(Long routeId, String imageType) throws IOException {
        RouteDomain route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada"));

        // Limpiar los datos de imagen y URL en la base de datos
        if ("outbound".equals(imageType)) {
            route.setOutboundImageData(null);
            route.setOutboundImageContentType(null);
            route.setOutboundImageUrl(null);
        } else if ("return".equals(imageType)) {
            route.setReturnImageData(null);
            route.setReturnImageContentType(null);
            route.setReturnImageUrl(null);
        } else {
            throw new IllegalArgumentException("Tipo de imagen inválido: " + imageType);
        }

        routeRepository.save(route);
    }

    /**
     * Actualizar URLs de imágenes para rutas existentes (método de migración)
     */
    public void updateExistingImageUrls() {
        List<RouteDomain> routes = routeRepository.findAll();
        String imageBaseUrl = determineImageBaseUrl();

        for (RouteDomain route : routes) {
            boolean updated = false;

            if (route.getOutboundImageUrl() != null && !route.getOutboundImageUrl().startsWith(imageBaseUrl)) {
                // Extraer filename de la URL antigua
                String filename = extractFilenameFromUrl(route.getOutboundImageUrl());
                if (filename != null) {
                    route.setOutboundImageUrl(imageBaseUrl + "/api/v1/routes/images/" + filename);
                    updated = true;
                }
            }

            if (route.getReturnImageUrl() != null && !route.getReturnImageUrl().startsWith(imageBaseUrl)) {
                // Extraer filename de la URL antigua
                String filename = extractFilenameFromUrl(route.getReturnImageUrl());
                if (filename != null) {
                    route.setReturnImageUrl(imageBaseUrl + "/api/v1/routes/images/" + filename);
                    updated = true;
                }
            }

            if (updated) {
                routeRepository.save(route);
            }
        }
    }

    public CrudResponseDto<RouteDetailsResDto> findByIdType(Long id, String type) {
        RouteDomain route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ruta con id " + id + " no encontrada."));

        boolean needsUpdate = false;

        // Generar URLs para imágenes existentes si no las tienen
        if (route.getOutboundImageData() != null && (route.getOutboundImageUrl() == null || route.getOutboundImageUrl().isEmpty())) {
            route.setOutboundImageUrl(generateImageUrl(route.getId(), "outbound"));
            needsUpdate = true;
        }
        if (route.getReturnImageData() != null && (route.getReturnImageUrl() == null || route.getReturnImageUrl().isEmpty())) {
            route.setReturnImageUrl(generateImageUrl(route.getId(), "return"));
            needsUpdate = true;
        }

        // Guardar la ruta si necesita actualización
        if (needsUpdate) {
            routeRepository.save(route);
        }

        List<RouteWaypointDomain> waypointDomains = routeWaypointRepository.findByRouteAndType(route, type);

        RouteDetailsResDto routeDetails = RouteMapper.toDtoDetail(route);

        routeDetails.setWaypoints(waypointDomains.stream()
                .map(RouteWaypointMapper::toDto)
                .toList());

        return CrudResponseDto.success(routeDetails, "Ruta Lista");
    }

    private String saveImageInternal(MultipartFile file, Integer routeNumber, String type) {
        if (file == null || file.isEmpty()) {
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
            // Guardar datos de imagen en memoria para almacenar en BD
            byte[] imageData = file.getBytes();

            // Retornar los datos de imagen y content type como un objeto compuesto
            // Para mantener compatibilidad con el código existente, retornamos null
            // y manejamos el almacenamiento en los métodos create/update
            return null;

        } catch (IOException e) {
            throw new RuntimeException("Error al procesar la imagen: " + e.getMessage());
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

    private void deleteIfExists(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) return;

        try {
            // Extraer filename de cualquier formato de URL
            String filename = extractFilenameFromUrl(publicUrl);
            if (filename != null) {
                Path path = Paths.get("src/main/resources/static/images/routes", filename);
                if (Files.exists(path)) {
                    Files.delete(path);
                }
            }
        } catch (IOException e) {
            // Log the error but don't fail the operation
            log.warn("Error deleting image file: {}", e.getMessage());
        }
    }

    /**
     * Extraer nombre del archivo desde cualquier formato de URL
     */
    private String extractFilenameFromUrl(String imageUrl) {
        if (imageUrl == null) return null;

        // Manejar diferentes formatos de URL:
        // - http://localhost:8080/api/v1/route/images/filename.ext
        // - /api/v1/route/images/filename.ext
        // - http://localhost:3000/api/v1/route/images/filename.ext

        String[] parts = imageUrl.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }

    /**
     * Determinar la URL base para imágenes basándose en el entorno
     */
    private String determineImageBaseUrl() {
        try {
            // Detectar si estamos ejecutándonos en Docker
            boolean isDocker = checkIfRunningInDocker();

            if (isDocker) {
                return "http://backend:8080";
            }

            // Para desarrollo, siempre usar localhost con el puerto del servidor
            return "http://localhost:" + serverPort;

        } catch (Exception e) {
            return baseUrl;
        }
    }

    /**
     * Detectar si estamos ejecutándonos en Docker
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

    private List<RouteWaypointReqDto> parseJson(String json) throws BadRequestException {
        List<RouteWaypointReqDto> waypointDtos;
        try {
            waypointDtos = objectMapper.readValue(
                    json, new TypeReference<List<RouteWaypointReqDto>>() {
                    });
        } catch (JsonProcessingException e) {
            throw new BadRequestException("El JSON de waypoints es inválido: " + e.getMessage());
        }

        return waypointDtos;
    }

    /**
     * Generar URL para acceder a una imagen de ruta
     */
    private String generateImageUrl(Long routeId, String imageType) {
        String baseUrl = determineImageBaseUrl();
        return baseUrl + "/api/v1/routes/" + routeId + "/images/" + imageType;
    }

}
