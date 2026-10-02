package no.loopacademy.models.actions;

import jakarta.persistence.Embeddable;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.items.Tool;
import no.loopacademy.models.items.Weapon;

/**
 * An item a survivor must carry to perform an action.
 * type is "tool" or "weapon", the same values as ItemResponse.type.
 * name is a specific item name, or null when any item of that type will do.
 */
@Embeddable
public class RequiredItem {
    private String type;
    private String name;

    protected RequiredItem() {
    }

    public RequiredItem(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    // Same type, and the same name (ignoring case) unless any item of that type will do
    public boolean isMetBy(Item item) {
        return type.equals(typeOf(item)) && (name == null || name.equalsIgnoreCase(item.getName()));
    }

    // Same strings ItemMapper uses for ItemResponse.type
    private static String typeOf(Item item) {
        if (item instanceof Tool) {
            return "tool";
        }
        if (item instanceof Weapon) {
            return "weapon";
        }
        return null;
    }
}
