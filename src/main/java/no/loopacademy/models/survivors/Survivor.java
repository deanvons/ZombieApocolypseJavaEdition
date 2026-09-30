package no.loopacademy.models.survivors;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.models.actions.Action;
import no.loopacademy.models.attributes.SurvivorAttributes;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.userprofile.UserProfile;

@Entity
public class Survivor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToOne(mappedBy = "survivor")
    private UserProfile user;

    @Column(nullable = false)
    @ElementCollection(targetClass = Skill.class)
    @Enumerated(EnumType.STRING)
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "survivor_id")
    private List<Item> gear = new ArrayList<>();

    @Embedded
    private SurvivorAttributes attributes;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SurvivorType type;

    protected Survivor() {
    }

    public Survivor(String name, SurvivorType type) {
        this.name = name;
        this.type = type;
        this.attributes = type.getDefaultAttributes();
        this.skills = new ArrayList<>(type.getDefaultSkills());
    }

    public void load(Item item) {
        double currentMaxLoadCapacity = getMaxLoad();
        double currentLoad = 0;

        for (Item i : gear) {
            currentLoad += i.getWeight();
        }

        if (currentLoad + item.getWeight() > currentMaxLoadCapacity) {
            throw new OverloadedException("Survivor with id: " + id + ", tried to load item with too much weight.");
        } else {
            gear.add(item);
        }
    }

    private double getMaxLoad() {
        return 10 + attributes.getStrength() * 3;
    }

    public double performAction(Action action) {

        double effectiveness = 0.0;

        double strengthContrib = attributes.getStrength() * action.getAttributeWeights().getStrength();
        double agilityContrib = attributes.getAgility() * action.getAttributeWeights().getAgility();
        double trustContrib = attributes.getTrustworthiness() * action.getAttributeWeights().getTrustworthiness();
        double intelContrib = attributes.getIntelligence() * action.getAttributeWeights().getIntelligence();
        double courContrib = attributes.getCourage() * action.getAttributeWeights().getCourage();
        double enduranceContrib = attributes.getEndurance() * action.getAttributeWeights().getEndurance();
        double leadContrib = attributes.getLeadership() * action.getAttributeWeights().getLeadership();

        effectiveness += (strengthContrib + agilityContrib + trustContrib + intelContrib + courContrib
                + enduranceContrib + leadContrib) * 10;

        return effectiveness;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public UserProfile getUser() {
        return user;
    }

    //Calling this directly changes nothing in the database, because only UserProfile.survivor is saved. 
    //Set survivor.user via UserProfile.setSurvivor instead.
    public void setUser(UserProfile user) {
        this.user = user;
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
