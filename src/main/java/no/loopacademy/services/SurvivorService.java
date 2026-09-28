package no.loopacademy.services;

import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.models.actions.Action;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SurvivorService {

    Map<Long, Survivor> survivors = new HashMap<Long, Survivor>();

    public Survivor create(String name, SurvivorType type) {
        Survivor survivor = switch (type) {
            case CAREGIVER -> new Survivor(name, type);
            default -> null;
        };
        long id = survivors.size() + 1L;

        assert survivor != null;
        survivor.setId(id);
        survivors.put(id, survivor);

        return survivor;
    }

    public List<Survivor> findAll() {
        return List.copyOf(survivors.values());
    }

    public Survivor findById(Long id) {
        Survivor survivor = survivors.get(id);

        if (survivor == null) {
            throw new SurvivorNotFoundException(
                    "Survivor with id: " + id + " not found");
        }
        return survivor;
    }

    public void addSkill(Long id, Skill skill) {
        Survivor survivor = findById(id);
        List<Skill> skills = new ArrayList<>(survivor.getSkills());
        if (skills.contains(skill)) {
            return;
        }
        skills.add(skill);
        survivor.setSkills(skills);
    }

    public void removeSkill(Long id, Skill skill) {
        Survivor survivor = findById(id);
        List<Skill> skills = new ArrayList<>(survivor.getSkills());
        skills.remove(skill);
        survivor.setSkills(skills);
    }

    public void loadItem(Long id, Item item) {
        Survivor survivor = findById(id);
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

    public double performAction(Long id, Action action) {
        Survivor survivor = findById(id);

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
