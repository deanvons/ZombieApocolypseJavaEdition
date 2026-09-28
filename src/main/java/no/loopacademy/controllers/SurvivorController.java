package no.loopacademy.controllers;

import no.loopacademy.models.actions.Action;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
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

    @PostMapping("/{id}")
    public void setSurvivorSkillsById(@PathVariable Long id, @RequestBody List<Skill> skills) {
        skills.addAll(survivorService.findById(id).getSkills());
        survivorService.findById(id).setSkills(skills);
    }

    @DeleteMapping("/{id}/skills/{skill}")
    public void DeleteSurvivorsSkillBYId(
            @PathVariable Long id, @PathVariable Skill skill
    ){
        survivorService.findById(id).getSkills().remove(skill);
    }

    @PostMapping("/{id}")
    public void setSurvivorsItem(@PathVariable Long id, @RequestBody List<Item> items) {
        items.addAll(survivorService.findById(id).getGear());
        survivorService.findById(id).setGear(items);
    }

    @PostMapping("/{id}/{actionId}")
    public void performAnActionByIdGetsEffectiveness(@PathVariable Long id, @PathVariable Integer actionId) {
        //TODO
        //ActionService in needed to accsess action with corresponding id, but not part of this ticket.
        //this is tempory solution untill we have that
        Action temporaryAction = null;
        survivorService.findById(id).performAction(temporaryAction);
    }
    
}
