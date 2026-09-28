package no.loopacademy.models.items;

import java.util.Objects;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity 
@DiscriminatorValue("weapon")
public class Weapon extends Item {
    int damage;

    public Weapon() {
    }

    public Weapon(String name, Double weight, int damage) {
        super(name, weight);
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Weapon weapon)) return false;
        if (!super.equals(o)) return false;
        return damage == weapon.damage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), damage);
    }
}
