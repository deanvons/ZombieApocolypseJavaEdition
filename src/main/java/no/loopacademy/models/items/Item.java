package no.loopacademy.models.items;

import java.util.Objects;

public class Item {
    String name;
    double weight;

    public Item(String name, Double weight) {
        this.name = name;
        this.weight = weight;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Item item)) return false;
        return Double.compare(weight, item.weight) == 0 && Objects.equals(name, item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, weight);
    }
}
