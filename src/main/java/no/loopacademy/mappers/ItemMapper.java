package no.loopacademy.mappers;

import java.util.List;
import java.util.Locale;

import org.mapstruct.Mapper;

import no.loopacademy.dtos.request.ItemLoadRequest;
import no.loopacademy.dtos.response.ItemResponse;
import no.loopacademy.exceptions.BusinessRuleException;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.items.Tool;
import no.loopacademy.models.items.Weapon;

// Written by hand, since Item is a hierarchy and the DTOs are flat with a "type" field
@Mapper(componentModel = "spring")
public interface ItemMapper {

    default ItemResponse toResponse(Item item) {
        return switch(item) {
            case null          -> null;
            case Tool tool     -> new ItemResponse(tool.getId(), "tool", tool.getName(), tool.getWeight(), tool.getDurability(), null);
            case Weapon weapon -> new ItemResponse(weapon.getId(), "weapon", weapon.getName(), weapon.getWeight(), null, weapon.getDamage());
            default            -> throw new IllegalStateException("Unknown item subclass: " + item.getClass().getSimpleName());
        };
    }

    List<ItemResponse> toResponse(List<Item> items);

    default Item toEntity(ItemLoadRequest request) {
        String type = request.type().trim().toLowerCase(Locale.ROOT);
        switch (type) {
            case "tool" -> {
                if (request.durability() == null) {
                    throw new BusinessRuleException("A tool needs a durability");
                }
                return new Tool(request.name(), request.weight(), request.durability());
            }
            case "weapon" -> {
                if (request.damage() == null) {
                    throw new BusinessRuleException("A weapon needs a damage value");
                }
                return new Weapon(request.name(), request.weight(), request.damage());
            }
            default -> throw new BusinessRuleException("Unknown item type: " + request.type() + ". Valid types are: [tool, weapon]");
        }
    }
    
}
