package no.loopacademy.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.loopacademy.dtos.response.ActionResponse;
import no.loopacademy.mappers.ActionMapper;
import no.loopacademy.models.actions.Action;
import no.loopacademy.services.ActionService;

@Tag(name = "Action", description="get all actions, get action by id")
@RestController 
@RequestMapping("/api/actions")
public class ActionController {

    private final ActionService actionService;
    private final ActionMapper actionMapper;

    public ActionController(
        ActionService actionService,
        ActionMapper actionMapper
    ) {
        this.actionService = actionService;
        this.actionMapper = actionMapper;
    }

    @Operation(
            summary = "Get all actions.",
            description = "Returns every action, with the items a survivor needs to perform it (requiredItems). "
                    + "An item's name is null when any item of that type will do."
    )
    @ApiResponse(responseCode = "200", description = "List of all actions.")
    @GetMapping
    public ResponseEntity<List<ActionResponse>> getAll() {
        List<Action> actions = actionService.findAll();
        return ResponseEntity.ok(actionMapper.toResponse(actions));
    } 

    @Operation(
            summary = "Get action by id.",
            description = "Returns the action with the given id, with the items a survivor needs to perform it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The action was found."),
            @ApiResponse(responseCode = "404", description = "No action exists with the given id.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ActionResponse> getById(@PathVariable Long id) {
        Action action = actionService.findById(id);
        return ResponseEntity.ok(actionMapper.toResponse(action));
    }
}
