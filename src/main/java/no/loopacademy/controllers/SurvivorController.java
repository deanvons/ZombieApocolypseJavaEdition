package no.loopacademy.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import no.loopacademy.dtos.request.ItemLoadRequest;
import no.loopacademy.dtos.request.SkillAddRequest;
import no.loopacademy.dtos.request.SurvivorCreateRequest;
import no.loopacademy.dtos.response.ActionResultResponse;
import no.loopacademy.dtos.response.SurvivorResponse;
import no.loopacademy.mappers.ItemMapper;
import no.loopacademy.mappers.SurvivorMapper;
import no.loopacademy.models.actions.ActionResult;
import no.loopacademy.models.actions.Action;
import no.loopacademy.dtos.request.CreateSurvivorRequest;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.services.SurvivorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/survivors")
public class SurvivorController {

    private final SurvivorService survivorService;
    private final SurvivorMapper survivorMapper;
    private final ItemMapper itemMapper;

    public SurvivorController(
        SurvivorService survivorService,
        SurvivorMapper survivorMapper,
        ItemMapper itemMapper
    ) {
        this.survivorService = survivorService;
        this.survivorMapper = survivorMapper;
        this.itemMapper = itemMapper;
    }

    @GetMapping
    public ResponseEntity<List<SurvivorResponse>> getSurvivors() {
        return ResponseEntity.ok(survivorMapper.toResponse(survivorService.findAll()));
    }

    @PostMapping
    public ResponseEntity<SurvivorResponse> createSurvivor(@Valid @RequestBody SurvivorCreateRequest request) {
        Survivor survivor = survivorService.create(request.name(), survivorMapper.toSurvivorType(request.type()));
        return ResponseEntity
            .created(URI.create("/api/survivors/" + survivor.getId()))
            .body(survivorMapper.toResponse(survivor));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SurvivorResponse> getSurvivorById(@PathVariable Long id) {
        Survivor survivor = survivorService.findById(id);
        return ResponseEntity.ok(survivorMapper.toResponse(survivor));
    }

    @PostMapping("/{id}/skills")
    public ResponseEntity<SurvivorResponse> addSkill(@PathVariable Long id, @Valid @RequestBody SkillAddRequest request) {
        survivorService.addSkill(id, request.skill());
        return ResponseEntity.ok(survivorMapper.toResponse(survivorService.findById(id)));
    }

    @DeleteMapping("/{id}/skills/{skill}")
    public ResponseEntity<Void> removeSkill(@PathVariable Long id, @PathVariable Skill skill) {
        survivorService.removeSkill(id, skill);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<SurvivorResponse> loadItem(@PathVariable Long id, @Valid @RequestBody ItemLoadRequest request) {
        survivorService.loadItem(id, itemMapper.toEntity(request));
        return ResponseEntity.ok(survivorMapper.toResponse(survivorService.findById(id)));
    }

    @PostMapping("/{id}/actions/{actionId}")
    public ResponseEntity<ActionResultResponse> performAction(@PathVariable Long id, @PathVariable Long actionId) {
        ActionResult result = survivorService.performAction(id, actionId);
        return ResponseEntity.ok(new ActionResultResponse(id, actionId, result.score()));
    }

}
