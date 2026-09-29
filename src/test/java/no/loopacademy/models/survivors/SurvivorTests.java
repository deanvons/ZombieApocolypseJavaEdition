package no.loopacademy.models.survivors;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import no.loopacademy.models.attributes.SurvivorAttributes;
import no.loopacademy.models.skills.Skill;

public class SurvivorTests {

    @Test
    void caregiverShouldBeCreatedWithCorrectName() {
        // Arrange
        String expectedName = "Melvin";
        SurvivorType survivorType = SurvivorType.CAREGIVER;
        // Act
        Survivor c = new Survivor(expectedName, survivorType);
        String actualName = c.getName();
        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    void caregiverShouldBeCreatedWithCorrectAttributes() {
        // Arrange
        SurvivorAttributes expectedAttributes = new SurvivorAttributes();
        expectedAttributes.setStrength(2);
        expectedAttributes.setEndurance(6);
        expectedAttributes.setAgility(3);
        expectedAttributes.setCourage(5);
        expectedAttributes.setIntelligence(8);
        expectedAttributes.setLeadership(5);
        expectedAttributes.setTrustworthiness(8);

        // Act
        String survivorName = "John";
        SurvivorType survivorType = SurvivorType.CAREGIVER;
        Survivor john = new Survivor(survivorName, survivorType);
        SurvivorAttributes actualAttributes = john.getAttributes();

        // Assert
        assertEquals(expectedAttributes, actualAttributes);
    }

    @Test
    void caregiverShouldBeCreatedWithCorrectSkills() {

        String survivorName = "John";
        SurvivorType survivorType = SurvivorType.CAREGIVER;
        Survivor john = new Survivor(survivorName, survivorType);
        List<Skill> expectedSkills = List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking);
        List<Skill> actualSkills = john.getSkills();

        // Assert
        assertEquals(expectedSkills, actualSkills);

    }
}
