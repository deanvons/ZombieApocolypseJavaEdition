package no.loopacademy.models.attributes;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class GeneralAttributes {
    // Physical Attributes
    @Column(nullable = false)
    private double strength;       // Ability to carry heavy items, push zombies, or use melee weapons.
    @Column(nullable = false)
    private double endurance;      // Resistance to injury, illness; travel/work longer without rest.
    @Column(nullable = false)
    private double agility;        // Reflexes for dodging or moving through tight spaces.

    // Mental Attributes
    @Column(nullable = false)
    private double courage;        // Resistance to fear; handle pressure and zombie hordes.
    @Column(nullable = false)
    private double intelligence;   // Learning, problem-solving, crafting.

    // Social Attributes
    @Column(nullable = false)
    private double leadership;     // Organizing, decision-making, inspiring the group.
    @Column(nullable = false)
    private double trustworthiness; // Relationships and willingness to share info/resources.

    public GeneralAttributes(double strength, double endurance, double agility, double courage, double intelligence, double leadership, double trustworthiness) {
        this.strength = strength;
        this.endurance = endurance;
        this.agility = agility;
        this.courage = courage;
        this.intelligence = intelligence;
        this.leadership = leadership;
        this.trustworthiness = trustworthiness;
    }

    public GeneralAttributes() {
    }

    public GeneralAttributes add(GeneralAttributes other) {
        return new GeneralAttributes(
                this.strength + other.strength,
                this.endurance + other.endurance,
                this.agility + other.agility,
                this.courage + other.courage,
                this.intelligence + other.intelligence,
                this.leadership + other.leadership,
                this.trustworthiness + other.trustworthiness
        );
    }

    // Getters and setters
    public double getStrength() { return strength; }
    public void setStrength(double strength) { this.strength = strength; }

    public double getEndurance() { return endurance; }
    public void setEndurance(double endurance) { this.endurance = endurance; }

    public double getAgility() { return agility; }
    public void setAgility(double agility) { this.agility = agility; }

    public double getCourage() { return courage; }
    public void setCourage(double courage) { this.courage = courage; }

    public double getIntelligence() { return intelligence; }
    public void setIntelligence(double intelligence) { this.intelligence = intelligence; }

    public double getLeadership() { return leadership; }
    public void setLeadership(double leadership) { this.leadership = leadership; }

    public double getTrustworthiness() { return trustworthiness; }
    public void setTrustworthiness(double trustworthiness) { this.trustworthiness = trustworthiness; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GeneralAttributes that = (GeneralAttributes) o;
        return Double.compare(strength, that.strength) == 0 && Double.compare(endurance, that.endurance) == 0 && Double.compare(agility, that.agility) == 0 && Double.compare(courage, that.courage) == 0 && Double.compare(intelligence, that.intelligence) == 0 && Double.compare(leadership, that.leadership) == 0 && Double.compare(trustworthiness, that.trustworthiness) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(strength, endurance, agility, courage, intelligence, leadership, trustworthiness);
    }
}
