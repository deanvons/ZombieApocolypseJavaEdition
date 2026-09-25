package items;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.items.Tool;
import no.loopacademy.models.items.Weapon;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.services.SurvivorService;


public class ItemTests {
    @BeforeEach
    public void setup() {
        // Setup objects to use as data
    }

    @Test
    void shouldCreateToolWithCorrectDetails() {
        // Arrange
        String expectedName = "Spanner";
        double expectedWeight = 10;
        int expectedDurability = 100;
        Tool tool = new Tool(expectedName,expectedWeight,expectedDurability);

        // Act
        String actualName = tool.getName();
        double actualWeight = tool.getWeight();
        int actualDurability = tool.getDurability();

        // Assert
        assertEquals(expectedName,actualName);
        assertEquals(expectedWeight,actualWeight);
        assertEquals(expectedDurability,actualDurability);
    }

    @Test
    void shouldCreateWeaponWithCorrectDetails() {
        // Arrange
        String expectedName = "Ak-47";
        double expectedWeight = 5;
        int expectedDamage = 10;

        Weapon w = new Weapon(expectedName,expectedWeight,expectedDamage);

        // Act
        String actualName = w.getName();
        double actualWeight = w.getWeight();
        int actualDamage = w.getDamage();

        // Assert
        assertEquals(expectedName,actualName);
        assertEquals(expectedWeight,actualWeight);
        assertEquals(expectedDamage,actualDamage);
    }

    @Test
    void shouldCreateSurvivorWithCorrectItems() {
        // Arrange
        Survivor john = new Survivor("John", SurvivorType.CAREGIVER);
        List<Item> expectedItems = new ArrayList<>();
        expectedItems.add(new Tool("Spanner", 10.0, 100));
        expectedItems.add(new Weapon("AK-47", 5.0, 10));

        // Act
        john.setGear(expectedItems);
        List<Item> actualItems = john.getGear();

        // Assert
        assertEquals(expectedItems,actualItems);
    }

    @Test
    void shouldBeAbleToLoadIfUnderWeightLimit() throws Exception {
        // Arrange
        SurvivorService service = new SurvivorService();
        Survivor john = service.create("John", SurvivorType.CAREGIVER);
        Tool tool = new Tool("Spanner", 10.0, 100);
        // Act
        service.loadItem(john.getId(), tool);
        // Assert - to check if he has it equipped
        assertEquals(1,  john.getGear().size());
    }

    @Test
    void shouldBeAbleToLoadItemsIfUnderWeightLimit() throws Exception {
        // Arrange
        SurvivorService service = new SurvivorService();
        Survivor john = service.create("John", SurvivorType.CAREGIVER);
        Tool tool = new Tool("Spanner", 10.0, 100);
        Weapon weapon = new Weapon("AK-47", 5.0, 10);
        // Act
        service.loadItem(john.getId(), tool);
        service.loadItem(john.getId(), weapon);
        // Assert - to check if he has it equipped
        assertEquals(2,  john.getGear().size());
    }

    @Test
    void loadShouldFailWithHeavyItem() throws Exception {
        // Arrange
        SurvivorService service = new SurvivorService();
        Survivor john = service.create("John", SurvivorType.CAREGIVER);
        Tool tool = new Tool("Spanner", 100.0, 100);
        // Act & assert
        assertThrows(OverloadedException.class, () -> service.loadItem(john.getId(), tool));
    }

    @Test
    void loadShouldFailWithHeavyItems() throws Exception {
        // Arrange
        SurvivorService service = new SurvivorService();
        Survivor john = service.create("John", SurvivorType.CAREGIVER);
        Tool tool = new Tool("Spanner", 10.0, 100);
        Weapon weapon = new Weapon("AK-47", 10.0, 10); // Too heavy
        // Act & assert
        service.loadItem(john.getId(), tool);
        assertThrows(OverloadedException.class, () -> service.loadItem(john.getId(), weapon));
    }

}
