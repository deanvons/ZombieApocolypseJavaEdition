package no.loopacademy.services;

import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.CareGiver;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorTypes;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SurvivorService {

    Map<Long, Survivor> survivors = new HashMap<Long, Survivor>();

    public Survivor create(String name, SurvivorTypes type) {
        Survivor survivor = switch (type) {
            case CAREGIVER -> new CareGiver(name);
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
        try {
            return survivors.get(id);
        } catch (NullPointerException e) {
            throw new SurvivorNotFoundException("Survivor with id:" + id + " Not Found");
        }
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

}
