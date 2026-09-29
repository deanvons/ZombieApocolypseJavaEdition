package no.loopacademy.controllers;

import no.loopacademy.models.actions.Action;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.services.ActionService;
import no.loopacademy.services.SurvivorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/survivors")
public class SurvivorController {

    @Autowired
    private SurvivorService survivorService;

    @Autowired
    private ActionService actionService;

    @GetMapping
    public List<Survivor> getSurvivors() {
        return survivorService.findAll();
    }

    @PostMapping
    public Survivor createSurvivor(@RequestBody String name, @RequestBody SurvivorType survivorType) {
        return survivorService.create(name, survivorType);
    }

    @GetMapping("/{id}")
    public Survivor getSurvivorById(@PathVariable Long id) {
        return survivorService.findById(id);
    }

    @PostMapping("/{id}/skills")
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

    @PostMapping("/{id}/items")
    public void setSurvivorsItem(@PathVariable Long id, @RequestBody List<Item> items) {
        items.addAll(survivorService.findById(id).getGear());
        survivorService.findById(id).setGear(items);
    }

    @PostMapping("/{id}/{actionId}")
    public double performAnActionByIdGetsEffectiveness(@PathVariable Long id, @PathVariable Long actionId) {
       
        Action actionToPerform = actionService.findById(actionId);
        Survivor survivor = survivorService.findById(id);
        double effectiveness = survivor.performAction(actionToPerform);
        return effectiveness;
    }
}
