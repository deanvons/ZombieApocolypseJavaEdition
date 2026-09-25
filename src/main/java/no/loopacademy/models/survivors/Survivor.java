package no.loopacademy.models.survivors;

import java.util.ArrayList;
import java.util.List;

import no.loopacademy.models.attributes.SurvivorAttributes;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;

public class Survivor {

    private Long id;

    private String name;

    private List<Skill> skills = new ArrayList<>();

    private List<Item> gear = new ArrayList<>();

    private SurvivorAttributes attributes;
    
    private SurvivorType type;


    public Survivor(String name, SurvivorType type) {
        this.name = name;
        this.type = type;
        this.attributes = type.getDefaultAttributes();
        this.skills = new ArrayList<>(type.getDefaultSkills());
    }

    // Getters and setters
    public Long getId(){ return id;}

    public void setId(Long id){this.id = id;}

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<Skill> getSkills() {
        return skills;
    }
    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Item> getGear() {
        return gear;
    }
    public void setGear(List<Item> gear) {
        this.gear = gear;
    }

    public SurvivorAttributes getAttributes() {
        return attributes;
    }
    public void setAttributes(SurvivorAttributes attributes) {
        this.attributes = attributes;
    }

    public SurvivorType getType() {
        return type;
    }
    public void setType(SurvivorType type) {
        this.type = type;
    }


}
