package no.loopacademy.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.services.SurvivorService;
import no.loopacademy.mappers.SurvivorMapper;

@RestController
@RequestMapping("/api/survivors")
public class SurvivorController {

    private final SurvivorService survivorService;
    private final SurvivorMapper surivovrMapper;

    public SurvivorController(SurvivorService survivorService, SurvivorMapper surivovrMapper) {
        this.survivorService = survivorService;
        this.surivovrMapper = surivovrMapper;
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<List<SurvivorResponse>> getSurvivor(@PathVariable Long id) {
        Survivor survivor = survivorService.findById(id);
        return ResponseEntity.ok(surivovrMapper.toResponse(survivor));
    }
}
