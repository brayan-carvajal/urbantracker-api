package com.sena.urbantracker.users.infrastructure.controller;

import com.sena.urbantracker.users.application.dto.request.DriverScheduleReqDto;
import com.sena.urbantracker.users.application.dto.response.DriverScheduleResDto;
import com.sena.urbantracker.users.application.service.DriverScheduleService;
import com.sena.urbantracker.shared.application.dto.CrudResponseDto;
import com.sena.urbantracker.shared.infrastructure.controller.BaseController;
import com.sena.urbantracker.shared.domain.enums.EntityType;
import com.sena.urbantracker.shared.application.service.ServiceFactory;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/driver-schedule")
public class DriverScheduleController extends BaseController<DriverScheduleReqDto, DriverScheduleResDto, Long> {

    private final DriverScheduleService driverScheduleService;

    public DriverScheduleController(ServiceFactory serviceFactory, DriverScheduleService driverScheduleService) {
        super(serviceFactory, EntityType.DRIVER_SCHEDULE, DriverScheduleReqDto.class, DriverScheduleResDto.class);
        this.driverScheduleService = driverScheduleService;
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CrudResponseDto<List<DriverScheduleResDto>>> createAll(@RequestBody List<DriverScheduleReqDto> dto) throws BadRequestException {
        CrudResponseDto<List<DriverScheduleResDto>> res = driverScheduleService.createAll(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PutMapping("/bulk/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CrudResponseDto<List<DriverScheduleResDto>>> updateAll(@PathVariable Long id, @RequestBody List<DriverScheduleReqDto> dto) throws BadRequestException {
        CrudResponseDto<List<DriverScheduleResDto>> res = driverScheduleService.updateAll(dto, id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(res);
    }

    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<CrudResponseDto<List<DriverScheduleResDto>>> getByDriverId(@PathVariable Long driverId) {
        CrudResponseDto<List<DriverScheduleResDto>> res = driverScheduleService.findByDriverId(driverId);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/driver/{driverId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CrudResponseDto<Void>> deleteByDriverId(@PathVariable Long driverId) {
        CrudResponseDto<Void> res = driverScheduleService.deleteByDriverId(driverId);
        return ResponseEntity.ok(res);
    }
}