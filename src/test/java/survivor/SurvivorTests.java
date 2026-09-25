package survivor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import no.loopacademy.exceptions.CarryWeightExceededException;
import no.loopacademy.models.attributes.SurvivorAttributes;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;

public class SurvivorTests {

    @Test
    void caregiverShouldBeCreatedWithCorrectName() {
        // Arrange
        String expectedName = "Melvin";
        // Act
        Survivor c = new Survivor(expectedName, SurvivorType.CAREGIVER);
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
        Survivor john = new Survivor("John", SurvivorType.CAREGIVER);
        SurvivorAttributes actualAttributes = john.getAttributes();

        // Assert
        assertEquals(expectedAttributes, actualAttributes);
    }

    @Test
    void caregiverShouldBeCreatedWithCorrectSkills() {

        Survivor john = new Survivor("John", SurvivorType.CAREGIVER);
        List<Skill> expectedSkills = List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking);
        List<Skill> actualSkills = john.getSkills();

        // Assert
        assertEquals(expectedSkills, actualSkills);

    }

    @Test
    void load_ShouldThrowCarryWeightExceededException_WhenLoadingAnItemThatExceedsMaxWeight() {
        // Arrange
        String expectedName = "Melvin";
        Double heavyItemweight = 9999.0;
        // Act
        Survivor c = new Survivor(expectedName, SurvivorType.CAREGIVER);
        Item testItem = new Item("Test item", heavyItemweight);

        // Assert
        assertThrows(CarryWeightExceededException.class, () -> c.load(testItem));
       

    }

}
