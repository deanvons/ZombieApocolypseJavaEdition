package no.loopacademy.services;

import no.loopacademy.exceptions.ActionNotFoundException;
import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.models.actions.Action;
import no.loopacademy.models.actions.ActionResult;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.repositories.ActionRepository;
import no.loopacademy.repositories.SurvivorRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SurvivorService {

    private final SurvivorRepository survivorRepository;
    private final ActionRepository actionRepository;

    public SurvivorService(SurvivorRepository survivorRepository, ActionRepository actionRepository) {
        this.survivorRepository = survivorRepository;
        this.actionRepository = actionRepository;
    }

    @Transactional
    public Survivor create(String name, SurvivorType type) {
        return survivorRepository.save(new Survivor(name, type));
    }

    @Transactional(readOnly = true)
    public List<Survivor> findAll() {
        List<Survivor> survivors = survivorRepository.findAll();
        survivors.forEach(this::initializeCollections);
        return survivors;
    }

    @Transactional
    public Survivor findById(Long id) {
        Survivor survivor = survivorRepository.findById(id).orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
        initializeCollections(survivor);
        return survivor;
    }

    // open-in-view is off, so lazy collections must be loaded before the transaction ends,
    // otherwise mapping to a DTO in the controller throws LazyInitializationException
    private void initializeCollections(Survivor survivor) {
        Hibernate.initialize(survivor.getSkills());
        Hibernate.initialize(survivor.getGear());
    }

    @Transactional
    public void addSkill(Long id, Skill skill) {
        Survivor survivor = survivorRepository.findById(id)
                .orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
        if (!survivor.getSkills().contains(skill)) {
            survivor.getSkills().add(skill);
        }
    }

    @Transactional
    public void removeSkill(Long id, Skill skill) {
        Survivor survivor = survivorRepository.findById(id)
                .orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
        survivor.getSkills().remove(skill);
    }

    @Transactional
    public void loadItem(Long id, Item item) {
        Survivor survivor = survivorRepository.findById(id)
                .orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
        double currentLoad = survivor.getGear().stream()
                .mapToDouble(Item::getWeight)
                .sum();

        if (currentLoad + item.getWeight() > getMaxLoad(survivor)) {
            throw new OverloadedException("Survivor with id: " + id + ", tried to load item with too much weight.");
        }
        survivor.getGear().add(item);

    }

    private double getMaxLoad(Survivor survivor) {
        return 10 + survivor.getAttributes().getStrength() * 3;
    }

    @Transactional(readOnly = true)
    public ActionResult performAction(Long id, Long actionId) {
        Survivor survivor = survivorRepository.findById(id)
                .orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
        Action action = actionRepository.findById(actionId)
                .orElseThrow(() -> new ActionNotFoundException("Action not found"));
        double effectiveness = 0.0;

        double strengthContrib = survivor.getAttributes().getStrength() * action.getAttributeWeights().getStrength();
        double agilityContrib = survivor.getAttributes().getAgility() * action.getAttributeWeights().getAgility();
        double trustContrib = survivor.getAttributes().getTrustworthiness()
                * action.getAttributeWeights().getTrustworthiness();
        double intelligenceContrib = survivor.getAttributes().getIntelligence()
                * action.getAttributeWeights().getIntelligence();
        double courageContrib = survivor.getAttributes().getCourage() * action.getAttributeWeights().getCourage();
        double enduranceContrib = survivor.getAttributes().getEndurance() * action.getAttributeWeights().getEndurance();
        double leadershipContrib = survivor.getAttributes().getLeadership()
                * action.getAttributeWeights().getLeadership();

        effectiveness = (strengthContrib + agilityContrib + trustContrib + intelligenceContrib
                + courageContrib + enduranceContrib + leadershipContrib) * 10;

        return new ActionResult(survivor, action, effectiveness);
    }

}
