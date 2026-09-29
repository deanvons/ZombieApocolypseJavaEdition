package no.loopacademy.models.items;

import java.util.Objects;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity 
@DiscriminatorValue("weapon")
public class Weapon extends Item {
    double damage;

    public Weapon() {
    }

    public Weapon(String name, Double weight, double damage) {
        super(name, weight);
        this.damage = damage;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Weapon weapon)) return false;
        if (!super.equals(o)) return false;
        return Double.compare(damage, weapon.damage) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), damage);
    }
}
