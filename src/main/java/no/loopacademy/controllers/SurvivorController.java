package no.loopacademy.controllers;

import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorTypes;
import no.loopacademy.services.SurvivorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/survivors")
public class SurvivorController {

    @Autowired
    private SurvivorService survivorService;

    @GetMapping
    public List<Survivor> getSurvivors() {
        return survivorService.findAll();
    }

    @PostMapping
    public Survivor createSurvivor(@RequestBody String name, @RequestBody SurvivorTypes survivorType) {
        return survivorService.create(name, survivorType);
    }

    @GetMapping("/{id}")
    public Survivor getSurvivorById(@PathVariable Long id) {
        return survivorService.findById(id);
    }


    
}
