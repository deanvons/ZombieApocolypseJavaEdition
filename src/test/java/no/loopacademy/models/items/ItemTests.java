package no.loopacademy.models.items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.loopacademy.repositories.ActionRepository;
import no.loopacademy.repositories.SurvivorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.services.SurvivorService;

public class ItemTests {
    private SurvivorService survivorService;
    private ActionRepository actionRepository;

    @BeforeEach
    public void setup() {
        SurvivorRepository repository = mock(SurvivorRepository.class);
        Map<Long, Survivor> survivors = new LinkedHashMap<>();
        AtomicLong nextId = new AtomicLong(1);
        actionRepository = mock(ActionRepository.class);

        when(repository.save(any(Survivor.class))).thenAnswer(invocation -> {
            Survivor survivor = invocation.getArgument(0);
            if (survivor.getId() == null) {
                survivor.setId(nextId.getAndIncrement());
            }
            survivors.put(survivor.getId(), survivor);
            return survivor;
        });
        when(repository.getReferenceById(any(Long.class)))
                .thenAnswer(invocation -> survivors.get(invocation.getArgument(0)));

        survivorService = new SurvivorService(repository, actionRepository);
    }

    @Test
    void shouldCreateToolWithCorrectDetails() {
        // Arrange
        String expectedName = "Spanner";
        double expectedWeight = 10;
        double expectedDurability = 100;
        Tool tool = new Tool(expectedName, expectedWeight, expectedDurability);

        // Act
        String actualName = tool.getName();
        double actualWeight = tool.getWeight();
        double actualDurability = tool.getDurability();

        // Assert
        assertEquals(expectedName, actualName);
        assertEquals(expectedWeight, actualWeight);
        assertEquals(expectedDurability, actualDurability);
    }

    @Test
    void shouldCreateWeaponWithCorrectDetails() {
        // Arrange
        String expectedName = "Ak-47";
        double expectedWeight = 5;
        double expectedDamage = 10;

        Weapon w = new Weapon(expectedName, expectedWeight, expectedDamage);

        // Act
        String actualName = w.getName();
        double actualWeight = w.getWeight();
        double actualDamage = w.getDamage();

        // Assert
        assertEquals(expectedName, actualName);
        assertEquals(expectedWeight, actualWeight);
        assertEquals(expectedDamage, actualDamage);
    }

    @Test
    void shouldCreateSurvivorWithCorrectItems() {
        // Arrange
        String survivorName = "John";
        SurvivorType survivorType = SurvivorType.CAREGIVER;
        Survivor john = new Survivor(survivorName, survivorType);
        List<Item> expectedItems = new ArrayList<>();
        String toolName = "Spanner";
        Double toolWeight = 10.0;
        double toolDurability = 100;
        expectedItems.add(new Tool(toolName, toolWeight, toolDurability));
        String weaponName = "AK-47";
        Double weaponWeight = 5.0;
        double weaponDamage = 10;
        expectedItems.add(new Weapon(weaponName, weaponWeight, weaponDamage));

        // Act
        john.setGear(expectedItems);
        List<Item> actualItems = john.getGear();

        // Assert
        assertEquals(expectedItems, actualItems);
    }

    @Test
    void shouldBeAbleToLoadIfUnderWeightLimit() throws Exception {
        // Arrange
        String survivorName = "John";
        SurvivorType survivorType = SurvivorType.CAREGIVER;
        Survivor john = survivorService.create(survivorName, survivorType);
        String toolName = "Spanner";
        Double toolWeight = 10.0;
        double toolDurability = 100;
        Tool tool = new Tool(toolName, toolWeight, toolDurability);
        int expectedGearItemCount = 1;
        // Act
        survivorService.loadItem(john.getId(), tool);
        // Assert - to check if he has it equipped
        assertEquals(expectedGearItemCount, john.getGear().size());
    }

    @Test
    void shouldBeAbleToLoadItemsIfUnderWeightLimit() throws Exception {
        // Arrange
        String survivorName = "John";
        SurvivorType survivorType = SurvivorType.CAREGIVER;
        Survivor john = survivorService.create(survivorName, survivorType);
        String toolName = "Spanner";
        Double toolWeight = 10.0;
        double toolDurability = 100;
        Tool tool = new Tool(toolName, toolWeight, toolDurability);
        String weaponName = "AK-47";
        Double weaponWeight = 5.0;
        double weaponDamage = 10;
        Weapon weapon = new Weapon(weaponName, weaponWeight, weaponDamage);
        int expectedGearItemCount = 2;
        // Act
        survivorService.loadItem(john.getId(), tool);
        survivorService.loadItem(john.getId(), weapon);
        // Assert - to check if he has it equipped
        assertEquals(expectedGearItemCount, john.getGear().size());
    }

    @Test
    void loadShouldFailWithHeavyItem() throws Exception {
        // Arrange
        String survivorName = "John";
        SurvivorType survivorType = SurvivorType.CAREGIVER;
        Survivor john = survivorService.create(survivorName, survivorType);
        String toolName = "Spanner";
        Double toolWeight = 100.0;
        double toolDurability = 100;
        Tool tool = new Tool(toolName, toolWeight, toolDurability);
        // Act & assert
        assertThrows(OverloadedException.class, () -> survivorService.loadItem(john.getId(), tool));
    }

    @Test
    void loadShouldFailWithHeavyItems() throws Exception {
        // Arrange
        String survivorName = "John";
        SurvivorType survivorType = SurvivorType.CAREGIVER;
        Survivor john = survivorService.create(survivorName, survivorType);
        String toolName = "Spanner";
        Double toolWeight = 10.0;
        double toolDurability = 100;
        Tool tool = new Tool(toolName, toolWeight, toolDurability);
        String weaponName = "AK-47";
        Double weaponWeight = 10.0;
        double weaponDamage = 10;
        Weapon weapon = new Weapon(weaponName, weaponWeight, weaponDamage); // Too heavy
        // Act & assert
        survivorService.loadItem(john.getId(), tool);
        assertThrows(OverloadedException.class, () -> survivorService.loadItem(john.getId(), weapon));
    }

}
