package no.loopacademy.models.survivors;

import no.loopacademy.models.attributes.SurvivorAttributes;
import no.loopacademy.models.skills.Skill;

import java.util.List;

public class CareGiver extends Survivor {

    public CareGiver(String name) {
        super(name);
        this.attributes = new SurvivorAttributes();
        attributes.setStrength(2);
        attributes.setEndurance(6);
        attributes.setAgility(3);
        attributes.setCourage(5);
        attributes.setIntelligence(8);
        attributes.setLeadership(5);
        attributes.setTrustworthiness(8);
        this.setSkills(List.of(Skill.FieldMedicine, Skill.PsychologicalSupport));
    }


    public SurvivorAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(SurvivorAttributes attributes) {
        this.attributes = attributes;
    }
}
