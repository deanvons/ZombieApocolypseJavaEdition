package no.loopacademy.services;

import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.models.actions.Action;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.repositories.SurvivorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SurvivorService {

   @Autowired
   private SurvivorRepository survivorRepository;

    public Survivor create(String name, SurvivorType type) {
        Survivor survivor = switch (type) {
            case CAREGIVER -> new Survivor(name, type);
            default -> null;
        };
        assert survivor != null;

        long id = survivorRepository.count()+1;
        survivor.setId(id);

        survivorRepository.save(survivor);

        return survivor;
    }

    public List<Survivor> findAll() {
        return survivorRepository.findAll();
    }

    public Survivor findById(Long id) {
       return survivorRepository.findById(id).orElseThrow(()->new SurvivorNotFoundException("Survivor not found"));
    }

    public void addSkill(Long id, Skill skill) {
        survivorRepository.getReferenceById(id).getSkills().add(skill);
    }

    public void removeSkill(Long id, Skill skill) {
        survivorRepository.getReferenceById(id).getSkills().remove(skill);
    }

    public void loadItem(Long id, Item item) {
        Survivor survivor = survivorRepository.getReferenceById(id);
        double currentLoad = survivor.getGear().stream()
                .mapToDouble(Item::getWeight)
                .sum();

        if (currentLoad + item.getWeight() > getMaxLoad(survivor)) {
            throw new OverloadedException("Survivor with id: " + id + ", tried to load item with too much weight.");
        }
        survivorRepository.getReferenceById(id).getGear().add(item);

    }

    private double getMaxLoad(Survivor survivor) {
        return 10 + survivorRepository
                .getReferenceById(survivor.getId()).getAttributes().getStrength() * 3;
    }

    public double performAction(Long id, Action action) {
        Survivor survivor = survivorRepository.getReferenceById(id);

        double strengthContrib = survivor.getAttributes().getStrength() * action.getAttributeWeights().getStrength();
        double agilityContrib = survivor.getAttributes().getAgility() * action.getAttributeWeights().getAgility();
        double trustContrib = survivor.getAttributes().getTrustworthiness() * action.getAttributeWeights().getTrustworthiness();
        double intelligenceContrib = survivor.getAttributes().getIntelligence() * action.getAttributeWeights().getIntelligence();
        double courageContrib = survivor.getAttributes().getCourage() * action.getAttributeWeights().getCourage();
        double enduranceContrib = survivor.getAttributes().getEndurance() * action.getAttributeWeights().getEndurance();
        double leadershipContrib = survivor.getAttributes().getLeadership() * action.getAttributeWeights().getLeadership();

        return (strengthContrib + agilityContrib + trustContrib + intelligenceContrib
                + courageContrib + enduranceContrib + leadershipContrib) * 10;

    }

}
