package no.loopacademy.models.actions;

import no.loopacademy.models.attributes.AttributeWeights;

public class Action {
    String name;
    ActionType type;
    String effect;
    String target = "";
    AttributeWeights attributeWeights;

    public Action(String name, ActionType type, String effect, String target, AttributeWeights attributeWeights) {
        this.name = name;
        this.type = type;
        this.effect = effect;
        this.target = target;
        this.attributeWeights = attributeWeights;
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
}
