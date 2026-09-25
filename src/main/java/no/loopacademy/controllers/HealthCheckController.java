package no.loopacademy.controllers;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

  private final JdbcTemplate jdbcTemplate;

  public HealthCheckController(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @GetMapping
  public ResponseEntity<Void> getHealthCheck() {
    return ResponseEntity.ok().build();
  }

    @GetMapping("new")
  public ResponseEntity<Void> getHealthCheckNew() {
    return ResponseEntity.ok().build();
  }

  // Runs a trivial query to prove the app can reach and log in to the database.
  // No tables needed: Postgres answers SELECT 1 on its own.
  @GetMapping("database")
  public ResponseEntity<String> getDatabaseHealthCheck() {
    try {
      jdbcTemplate.queryForObject("SELECT 1", Integer.class);
      return ResponseEntity.ok("Database connected");
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Database unreachable");
    }
  }
}
