package no.loopacademy.models.survivors;

import no.loopacademy.models.attributes.SurvivorAttributes;
import no.loopacademy.models.skills.Skill;

import java.util.List;

public class TestSurvivor extends Survivor {


    public TestSurvivor(String name) {
        super(name);
        this.attributes = new SurvivorAttributes();
        attributes.setStrength(10);
        attributes.setEndurance(10);
        attributes.setAgility(10);
        attributes.setCourage(10);
        attributes.setIntelligence(10);
        attributes.setLeadership(10);
        attributes.setTrustworthiness(10);
        this.setSkills(List.of(Skill.WeaponMaintenance, Skill.BluntWeapons));
    }


    public SurvivorAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(SurvivorAttributes attributes) {
        this.attributes = attributes;
    }

}
