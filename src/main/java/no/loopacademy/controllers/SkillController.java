package no.loopacademy.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.loopacademy.models.skills.Skill;


@Tag(name = "Skills", description="Get all skills")
@RestController 
@RequestMapping("/api/skills")
public class SkillController {

    @Operation(
            summary = "Get all skills.",
            description = "Returns every Skill a survivor can have."
    )
    @ApiResponse(responseCode = "200", description = "List of all skill names.")
    @GetMapping
    public ResponseEntity<List<Skill>> getAll() {   //No DTO because Skills are enum values serialized to strings.
        return ResponseEntity.ok(List.of(Skill.values()));
    } 

}
