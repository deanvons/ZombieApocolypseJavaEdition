package no.loopacademy.services;

import no.loopacademy.exceptions.ActionNotFoundException;
import no.loopacademy.models.actions.Action;
import no.loopacademy.models.actions.ActionType;
import no.loopacademy.models.attributes.AttributeWeights;
import no.loopacademy.repositories.ActionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class ActionService {

    private final ActionRepository actionRepository;

    public ActionService(ActionRepository actionRepository) {
        this.actionRepository = actionRepository;
    }

    @PostConstruct
    void seedActions() {
        if (actionRepository.count() != 0) {
            return;
        }

        actionRepository.saveAll(List.of(
            action(
                "Attack",
                ActionType.Attack,
                "Deal damage to a threat",
                "Enemy",
                weights(0.6, 0.2, 0.0, 0.0, 0.1, 0.1, 0.0)
            ),

            action(
                "Heal",
                ActionType.Heal,
                "Restore health to a survivor",
                "Survivor",
                weights(0.0, 0.1, 0.4, 0.4, 0.0, 0.1, 0.0)
            ),

            action(
                "Forage",
                ActionType.Forage,
                "Search for food and other necessities",
                "Location",
                weights(0.1, 0.4, 0.1, 0.3, 0.0, 0.1, 0.0)
            ),

            action(
                "Build Shelter",
                ActionType.Build,
                "Build a safe shelter",
                "Location",
                weights(0.4, 0.1, 0.0, 0.2, 0.1, 0.2, 0.0)
            ),

            action(
                "Persuade",
                ActionType.Persuade,
                "Convince another person",
                "Person",
                weights(0.0, 0.1, 0.4, 0.1, 0.0, 0.0, 0.4)
            )
        ));
    }

    private Action action(
            String name,
            ActionType type,
            String effect,
            String target,
            AttributeWeights attributeWeights
    ) {
        return new Action(name, type, effect, target, attributeWeights);
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

    @Transactional(readOnly = true)
    public List<Action> findAll() {
        return actionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Action findById(Long id) {
        return actionRepository.findById(id).orElseThrow(()->new ActionNotFoundException("Action not found"));
    }
}
