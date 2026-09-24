package survivor;

import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.CareGiver;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.exceptions.CarryWeightExceededException;
import no.loopacademy.models.attributes.SurvivorAttributes;
import no.loopacademy.models.items.Item;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SurvivorTests {

    @Test
    void caregiverShouldBeCreatedWithCorrectName() {
        // Arrange
        String expectedName = "Melvin";
        // Act
        CareGiver c = new CareGiver(expectedName);
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
        CareGiver john = new CareGiver("John");
        SurvivorAttributes actualAttributes = john.getAttributes();

        // Assert
        assertEquals(expectedAttributes, actualAttributes);
    }

    @Test
    void caregiverShouldBeCreatedWithCorrectSkills() {

        CareGiver john = new CareGiver("John");
        List<Skill> expectedSkills = List.of(Skill.FieldMedicine, Skill.PsychologicalSupport);
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
        CareGiver c = new CareGiver(expectedName);
        Item testItem = new Item("Test item", heavyItemweight);

        // Assert
        assertThrows(CarryWeightExceededException.class, () -> c.load(testItem));
       

    }

}
