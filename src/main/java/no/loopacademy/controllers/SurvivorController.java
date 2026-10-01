package no.loopacademy.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
import no.loopacademy.dtos.response.ItemResponse;
import no.loopacademy.dtos.response.SurvivorResponse;
import no.loopacademy.mappers.ItemMapper;
import no.loopacademy.mappers.SurvivorMapper;
import no.loopacademy.models.actions.ActionResult;
import no.loopacademy.models.attributes.SurvivorAttributes;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.userprofile.UserProfile;
import no.loopacademy.services.SurvivorService;
import no.loopacademy.services.UserProfileService;

@RestController
@RequestMapping("/api/survivors")
public class SurvivorController {

    private final SurvivorService survivorService;
    private final UserProfileService userProfileService;
    private final SurvivorMapper survivorMapper;
    private final ItemMapper itemMapper;

    public SurvivorController(
        SurvivorService survivorService,
        UserProfileService userProfileService,
        SurvivorMapper survivorMapper,
        ItemMapper itemMapper
    ) {
        this.survivorService = survivorService;
        this.userProfileService = userProfileService;
        this.survivorMapper = survivorMapper;
        this.itemMapper = itemMapper;
    }

    @GetMapping
    public ResponseEntity<List<SurvivorResponse>> getSurvivors() {
        return ResponseEntity.ok(survivorMapper.toResponse(survivorService.findAll()));
    }

    @PostMapping
    public ResponseEntity<SurvivorResponse> createSurvivor(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SurvivorCreateRequest request) {
        UserProfile user = userProfileService.findByKeycloakId(jwt.getSubject()); // 404 if no profile
        Survivor survivor = survivorService.create(user, request.name(), survivorMapper.toSurvivorType(request.type()));
        return ResponseEntity
            .created(URI.create("/api/survivors/" + survivor.getId()))
            .body(survivorMapper.toResponse(survivor));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SurvivorResponse> getSurvivorById(@PathVariable Long id) {
        Survivor survivor = survivorService.findById(id);
        return ResponseEntity.ok(survivorMapper.toResponse(survivor));
    }

    @GetMapping("/me")
    public ResponseEntity<SurvivorResponse> getMySurvivor(@AuthenticationPrincipal Jwt jwt) {
        UserProfile user = userProfileService.findByKeycloakId(jwt.getSubject()); // 404 if no profile
        Survivor survivor = survivorService.findByUser(user);
        return ResponseEntity.ok(survivorMapper.toResponse(survivor));
    }

    @GetMapping("/{id}/gear")
    public ResponseEntity<List<ItemResponse>> getSurvivorGear(@PathVariable Long id) {
        Survivor survivor = survivorService.findById(id);
        return ResponseEntity.ok(itemMapper.toResponse(survivor.getGear()));
    }

    @GetMapping("/{id}/attributes")
    public ResponseEntity<SurvivorAttributes> getSurvivorAttributes(@PathVariable Long id) {
        Survivor survivor = survivorService.findById(id);
        return ResponseEntity.ok(survivor.getAttributes());
    }

    @GetMapping("/{id}/skills")
    public ResponseEntity<List<Skill>> getSurvivorSkills(@PathVariable Long id) {
        Survivor survivor = survivorService.findById(id);
        return ResponseEntity.ok(survivor.getSkills());
    }

    // One skill per request, e.g. {"skill": "Cooking"}
    @PostMapping("/{id}/skills")
    public ResponseEntity<SurvivorResponse> addSkill(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
            @Valid @RequestBody SkillAddRequest request) {
        UserProfile user = userProfileService.findByKeycloakId(jwt.getSubject()); // 404 if no profile
        survivorService.addSkill(user, id, request.skill());
        return ResponseEntity.ok(survivorMapper.toResponse(survivorService.findById(id)));
    }

    @DeleteMapping("/{id}/skills/{skill}")
    public ResponseEntity<Void> removeSkill(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
            @PathVariable Skill skill) {
        UserProfile user = userProfileService.findByKeycloakId(jwt.getSubject()); // 404 if no profile
        survivorService.removeSkill(user, id, skill);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<SurvivorResponse> loadItem(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
            @Valid @RequestBody ItemLoadRequest request) {
        UserProfile user = userProfileService.findByKeycloakId(jwt.getSubject()); // 404 if no profile
        survivorService.loadItem(user, id, itemMapper.toEntity(request));
        return ResponseEntity.ok(survivorMapper.toResponse(survivorService.findById(id)));
    }

    @PostMapping("/{id}/actions/{actionId}")
    public ResponseEntity<ActionResultResponse> performAction(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
            @PathVariable Long actionId) {
        UserProfile user = userProfileService.findByKeycloakId(jwt.getSubject()); // 404 if no profile
        ActionResult result = survivorService.performAction(user, id, actionId);
        return ResponseEntity.ok(new ActionResultResponse(id, actionId, result.score()));
    }

}
