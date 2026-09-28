package no.loopacademy.services;

import no.loopacademy.exceptions.ActionNotFoundException;
import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.models.actions.Action;
import no.loopacademy.models.actions.ActionType;
import no.loopacademy.models.attributes.AttributeWeights;
import no.loopacademy.repositories.ActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ActionService {

    @Autowired
    private ActionRepository actionRepository;

    public ActionService() {
        addAction(
                "Attack",
                ActionType.Attack,
                "Deal damage to a threat",
                "Enemy",
                weights(0.6, 0.2, 0.0, 0.0, 0.1, 0.1, 0.0)
        );

        addAction(
                "Heal",
                ActionType.Heal,
                "Restore health to a survivor",
                "Survivor",
                weights(0.0, 0.1, 0.4, 0.4, 0.0, 0.1, 0.0)
        );

        addAction(
                "Scavenge",
                ActionType.Scavenge,
                "Find useful supplies",
                "Location",
                weights(0.1, 0.4, 0.1, 0.3, 0.0, 0.1, 0.0)
        );

        addAction(
                "Build Shelter",
                ActionType.Build,
                "Build a safe shelter",
                "Location",
                weights(0.4, 0.1, 0.0, 0.2, 0.1, 0.2, 0.0)
        );

        addAction(
                "Persuade",
                ActionType.Persuade,
                "Convince another person",
                "Person",
                weights(0.0, 0.1, 0.4, 0.1, 0.0, 0.0, 0.4)
        );
    }

    private void addAction(
            String name,
            ActionType type,
            String effect,
            String target,
            AttributeWeights attributeWeights
    ) {
        Action action = new Action(name, type, effect, target, attributeWeights);
        action.setId((long) actionRepository.count() + 1);
        actionRepository.save(action);
    }

    private AttributeWeights weights(
            double strength,
            double agility,
            double trustworthiness,
            double intelligence,
            double courage,
            double endurance,
            double leadership
    ) {
        AttributeWeights weights = new AttributeWeights();
        weights.setStrength(strength);
        weights.setAgility(agility);
        weights.setTrustworthiness(trustworthiness);
        weights.setIntelligence(intelligence);
        weights.setCourage(courage);
        weights.setEndurance(endurance);
        weights.setLeadership(leadership);
        return weights;
    }

    public List<Action> findAll() {
        return actionRepository.findAll();
    }

    public Action findById(Long id) {
        return actionRepository.findById(id).orElseThrow(()->new ActionNotFoundException("Action not found"));
    }
}