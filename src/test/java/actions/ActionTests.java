package actions;

import no.loopacademy.models.actions.Action;
import no.loopacademy.models.actions.ActionType;
import no.loopacademy.models.attributes.AttributeWeights;
import no.loopacademy.models.survivors.CareGiver;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.TestSurvivor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActionTests {
    @Test
    void shouldCreateActionWithCorrectValues() {
        // Arr
        String expectedName = "Drug";
        ActionType expectedActionType = ActionType.Disable;
        String expectedEffect = "Poes toe";
        String expectedTarget = "Yo mamma";
        AttributeWeights expectedAttributeWeights = new AttributeWeights();
        expectedAttributeWeights.setStrength(0.1);
        expectedAttributeWeights.setAgility(0.0);
        expectedAttributeWeights.setTrustworthiness(0.6);
        expectedAttributeWeights.setIntelligence(0.1);
        expectedAttributeWeights.setCourage(0.2);
        expectedAttributeWeights.setEndurance(0.0);
        expectedAttributeWeights.setLeadership(0.0);
        // Act
        Action a = new Action(expectedName,
                expectedActionType,
                expectedEffect,
                expectedTarget,
                expectedAttributeWeights);
        String actualName = a.getName();
        ActionType actualActionType = a.getType();
        AttributeWeights actualAttributeWeights = a.getAttributeWeights();
        String actualEffect = a.getEffect();
        String actualTarget = a.getTarget();
        // Ass
        assertEquals(expectedName, actualName);
        assertEquals(expectedActionType, actualActionType);
        assertEquals(expectedEffect, actualEffect);
        assertEquals(expectedTarget, actualTarget);
        assertEquals(expectedAttributeWeights, actualAttributeWeights);
    }



    @Test
    void shouldCalculateCorrectEffectivenessWithoutSkills() {
        double expectedEffectiveness = 100;
        Survivor john = new TestSurvivor("Testy");
        AttributeWeights drugWeights = new AttributeWeights();
        drugWeights.setStrength(0.1);
        drugWeights.setAgility(0.1);
        drugWeights.setTrustworthiness(0.1);
        drugWeights.setIntelligence(0.1);
        drugWeights.setCourage(0.1);
        drugWeights.setEndurance(0.1);
        drugWeights.setLeadership(0.4);
        Action drug = new Action("test", ActionType.Fix, "", "", drugWeights);

        double actualEffectiveness = john.performAction(drug);

        assertEquals(expectedEffectiveness,actualEffectiveness);
    }


}
