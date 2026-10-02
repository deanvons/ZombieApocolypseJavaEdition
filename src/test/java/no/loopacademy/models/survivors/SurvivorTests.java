package no.loopacademy.models.survivors;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.loopacademy.models.attributes.SurvivorAttributes;
import no.loopacademy.models.skills.Skill;

public class SurvivorTests {
    private String survivorName;
    private SurvivorType survivorType;

    @BeforeEach
    void setup() {
        survivorName = "GenericSurvivorName";
        survivorType = SurvivorType.CAREGIVER;
    }

    @Test
    void caregiverShouldBeCreatedWithCorrectName() {
        // Arrange
        String expectedName = survivorName;
        // Act
        Survivor c = new Survivor(survivorName, survivorType);
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
        Survivor john = new Survivor(survivorName, survivorType);
        SurvivorAttributes actualAttributes = john.getAttributes();

        // Assert
        assertEquals(expectedAttributes, actualAttributes);
    }

    @Test
    void caregiverShouldBeCreatedWithCorrectSkills() {

        Survivor john = new Survivor(survivorName, survivorType);
        List<Skill> expectedSkills = List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking);
        List<Skill> actualSkills = john.getSkills();

        // Assert
        assertEquals(expectedSkills, actualSkills);

    }

    @Test
    void leaderShouldBeCreatedWithCorrectAttributes() {
        // Arrange
        SurvivorAttributes expectedAttributes = new SurvivorAttributes();
        expectedAttributes.setStrength(10);
        expectedAttributes.setEndurance(10);
        expectedAttributes.setAgility(10);
        expectedAttributes.setCourage(10);
        expectedAttributes.setIntelligence(10);
        expectedAttributes.setLeadership(10);
        expectedAttributes.setTrustworthiness(10);

        // Act
        Survivor leader = new Survivor(survivorName, SurvivorType.LEADER);
        SurvivorAttributes actualAttributes = leader.getAttributes();

        // Assert
        assertEquals(expectedAttributes, actualAttributes);
    }

    @Test
    void leaderShouldBeCreatedWithCorrectSkills() {
        // Arrange
        List<Skill> expectedSkills = List.of(Skill.Leadership, Skill.MotivationalSpeaking, Skill.Negotiation);

        // Act
        Survivor leader = new Survivor(survivorName, SurvivorType.LEADER);
        List<Skill> actualSkills = leader.getSkills();

        // Assert
        assertEquals(expectedSkills, actualSkills);
    }
}
