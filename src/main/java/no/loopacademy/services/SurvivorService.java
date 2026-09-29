package no.loopacademy.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.models.actions.Action;
import no.loopacademy.models.actions.ActionResult;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.repositories.ActionRepository;
import no.loopacademy.repositories.SurvivorRepository;

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
        return survivorRepository.findAll();
    }

    @Transactional
    public Survivor findById(Long id) {
        return survivorRepository.findById(id).orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
    }

    @Transactional
    public void addSkill(Long id, Skill skill) {
        Survivor survivor = survivorRepository.getReferenceById(id);
        if (!survivor.getSkills().contains(skill)) {
            survivor.getSkills().add(skill);
        }
    }

    @Transactional
    public void removeSkill(Long id, Skill skill) {
        survivorRepository.getReferenceById(id).getSkills().remove(skill);
    }

    @Transactional
    public void loadItem(Long id, Item item) {
        survivorRepository.getReferenceById(id).load(item);
    }

    @Transactional(readOnly = true)
    public ActionResult performAction(Long id, Long actionId) {
        Survivor survivor = survivorRepository.getReferenceById(id);
        Action action = actionRepository.getReferenceById(actionId);
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
