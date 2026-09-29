package no.loopacademy.models.items;

import java.util.Objects;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("tool")
public class Tool extends Item {
    double durability;

    public Tool() {
    }

    public Tool(String name, Double weight, double durability) {
        super(name, weight);
        this.durability = durability;
    }

    public double getDurability() {
        return durability;
    }

    public void setDurability(double durability) {
        this.durability = durability;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tool tool)) return false;
        if (!super.equals(o)) return false;
        return Double.compare(durability, tool.durability) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), durability);
    }
}
