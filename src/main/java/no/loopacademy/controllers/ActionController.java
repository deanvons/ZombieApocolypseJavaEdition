package no.loopacademy.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import no.loopacademy.models.actions.Action;
import no.loopacademy.services.ActionService;

//TODO: Import DTOs.


@Tag(name = "action", description="get all actions, get action by id")
@RestController 
@RequestMapping("/api/actions")
public class ActionController {

    private final ActionService actionService;
    //TODO: Add swagger comments

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

        //TODO GET action


    @GetMapping
    public ResponseEntity<List<ActionResponse>> getAll() {
        List<Action> actions = actionService.findAll();
        //TODO: Map and return response
    } 

    @GetMapping("/{actionId}")
    public ResponseEntity<ActionResponse> getById(int actionId) {
        //TODO: Map and return response
    }
}
