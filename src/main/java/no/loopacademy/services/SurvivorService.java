package no.loopacademy.services;

import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.exceptions.SurvivorNotFoundException;
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
        try {
            Survivor survivor = findById(id);
            survivor.load(item);
        } catch (Exception e) {
            throw new OverloadedException("Survivor with id: " + id + ", tried to load item with too much weight.");
        }

    }

}
