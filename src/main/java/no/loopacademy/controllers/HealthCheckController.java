package no.loopacademy.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@RequestMapping("/healthcheck")
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<Map<String, String>> getHealthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
