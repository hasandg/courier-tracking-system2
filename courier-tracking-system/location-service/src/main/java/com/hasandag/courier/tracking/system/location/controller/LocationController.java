package com.hasandag.courier.tracking.system.location.controller;


import com.hasandag.courier.tracking.system.location.dto.LocationRequest;
import com.hasandag.courier.tracking.system.location.model.CourierLocation;
import com.hasandag.courier.tracking.system.location.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<CourierLocation> recordLocation(@Valid @RequestBody LocationRequest request) {
        log.info("Received location request: {}", request);
        CourierLocation location = locationService.recordLocation(request);
        return new ResponseEntity<>(location, HttpStatus.CREATED);
    }

    @GetMapping("/couriers/{courierId}")
    public ResponseEntity<Page<CourierLocation>> getCourierLocations(
            @PathVariable String courierId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CourierLocation> locations = locationService.getRecentLocations(courierId, pageable);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/couriers/{courierId}/history")
    public ResponseEntity<List<CourierLocation>> getLocationHistory(@PathVariable String courierId) {
        List<CourierLocation> history = locationService.getLocationHistory(courierId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/couriers")
    public ResponseEntity<List<String>> getAllCourierIds() {
        List<String> courierIds = locationService.getAllCourierIds();
        return ResponseEntity.ok(courierIds);
    }

    @GetMapping("/couriers/{courierId}/timerange")
    public ResponseEntity<List<CourierLocation>> getLocationsByTimeRange(
            @PathVariable String courierId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        List<CourierLocation> locations = locationService.getLocationsByTimeRange(courierId, startTime, endTime);
        return ResponseEntity.ok(locations);
    }
}
