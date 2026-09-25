package no.loopacademy.models.survivors;

import java.util.List;

import no.loopacademy.models.attributes.SurvivorAttributes;
import no.loopacademy.models.skills.Skill;

public enum SurvivorType{

    CAREGIVER(
        createAttributes(2,3,8,8,5,6,5),
        List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking)
    ),

    HERO(
        createAttributes(9,5,2,3,8,6,6),
        List.of(Skill.Marksman, Skill.HeavyWeapons, Skill.BluntWeapons)
    ),

    OUTLAW(
        createAttributes(7,9,1,5,6,6,3),
        List.of(Skill.ImprovisedCombat, Skill.StealthCombat, Skill.Intimidation, Skill.TrapSetting)

    ),
    
    TESTSURVIVOR(
        createAttributes(10,10,10,10,10,10,10),
        List.of(Skill.WeaponMaintenance, Skill.BluntWeapons)
    );



    private final SurvivorAttributes defaultAttributes;
    private final List<Skill> defaultSkills;

    private SurvivorType(SurvivorAttributes defaultAttributes, List<Skill> defaultSkills) {
        this.defaultAttributes = defaultAttributes;
        this.defaultSkills = defaultSkills;
    }

    // Returns a fresh copy so survivors of the same type don't share (and mutate) one attributes object
    public SurvivorAttributes getDefaultAttributes() {
        return createAttributes(
            defaultAttributes.getStrength(),
            defaultAttributes.getAgility(),
            defaultAttributes.getTrustworthiness(),
            defaultAttributes.getIntelligence(),
            defaultAttributes.getCourage(),
            defaultAttributes.getEndurance(),
            defaultAttributes.getLeadership()
        );
    }

    public List<Skill> getDefaultSkills() {
        return defaultSkills;
    }

    private static SurvivorAttributes createAttributes(
        double strength, double agility, double trustworthiness, double intelligence, double courage, double endurance, double leadership) {

            SurvivorAttributes attributes = new SurvivorAttributes();
            attributes.setStrength(strength);
            attributes.setAgility(agility);
            attributes.setTrustworthiness(trustworthiness);
            attributes.setIntelligence(intelligence);
            attributes.setCourage(courage);
            attributes.setEndurance(endurance);
            attributes.setLeadership(leadership);

            return attributes;

    }

    
    

}