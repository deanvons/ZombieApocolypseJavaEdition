package no.loopacademy.models.actions;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
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
import no.loopacademy.models.attributes.AttributeWeights;
import no.loopacademy.models.items.Item;

@Entity
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private ActionType type;
    private String effect;
    private String target = "";

    @Embedded
    private AttributeWeights attributeWeights;

    // Empty means any survivor can perform the action
    @ElementCollection
    @CollectionTable(name = "action_required_item", joinColumns = @JoinColumn(name = "action_id"))
    private List<RequiredItem> requiredItems = new ArrayList<>();

    protected Action() {
    }

    public Action(String name, ActionType type, String effect, String target, AttributeWeights attributeWeights) {
        this.name = name;
        this.type = type;
        this.effect = effect;
        this.target = target;
        this.attributeWeights = attributeWeights;
    }

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

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public AttributeWeights getAttributeWeights() {
        return attributeWeights;
    }

    public void setAttributeWeights(AttributeWeights attributeWeights) {
        this.attributeWeights = attributeWeights;
    }

    public List<RequiredItem> getRequiredItems() {
        return requiredItems;
    }

    public void setRequiredItems(List<RequiredItem> requiredItems) {
        this.requiredItems = requiredItems;
    }

    // Every requirement must be met by at least one item in the gear
    public boolean canBePerformedWith(List<Item> gear) {
        return requiredItems.stream()
                .allMatch(requirement -> gear.stream().anyMatch(requirement::isMetBy));
    }
}
