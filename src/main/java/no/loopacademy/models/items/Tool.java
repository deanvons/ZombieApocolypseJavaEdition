package no.loopacademy.models.items;

import java.util.Objects;

public class Tool extends Item {
    int durability;

    public Tool(String name, Double weight, Integer durability) {
        super(name, weight);
        this.durability = durability;
    }

    public Integer getDurability() {
        return durability;
    }

    public void setDurability(Integer durability) {
        this.durability = durability;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tool tool)) return false;
        if (!super.equals(o)) return false;
        return durability == tool.durability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), durability);
    }
}
