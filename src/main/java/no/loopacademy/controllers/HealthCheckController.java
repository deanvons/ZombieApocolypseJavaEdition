package no.loopacademy.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

  @GetMapping
  public ResponseEntity<Void> getHealthCheck() {
    return ResponseEntity.ok().build();
  }

    @GetMapping("new")
  public ResponseEntity<Void> getHealthCheck() {
    return ResponseEntity.ok().build();
  }
}
