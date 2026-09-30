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
    private String survivorName;
    private SurvivorType survivorType;
    private String toolName;
    private Double toolWeight;
    private double toolDurability;
    private String weaponName;
    private Double weaponWeight;
    private double weaponDamage;

    @BeforeEach
    public void setup() {
        survivorName = "GenericSurvivorName";
        survivorType = SurvivorType.CAREGIVER;
        toolName = "GenericToolName";
        toolWeight = 10.0;
        toolDurability = 100;
        weaponName = "GenericWeaponName";
        weaponWeight = 5.0;
        weaponDamage = 10;

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
        when(repository.findById(any(Long.class)))
                .thenAnswer(invocation -> java.util.Optional.ofNullable(survivors.get(invocation.getArgument(0))));

        survivorService = new SurvivorService(repository, actionRepository);
    }

    @Test
    void shouldCreateToolWithCorrectDetails() {
        // Arrange
        String expectedName = toolName;
        double expectedWeight = toolWeight;
        double expectedDurability = toolDurability;
        Tool tool = new Tool(toolName, toolWeight, toolDurability);

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
        String expectedName = weaponName;
        double expectedWeight = weaponWeight;
        double expectedDamage = weaponDamage;

        Weapon w = new Weapon(weaponName, weaponWeight, weaponDamage);

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
        Survivor john = new Survivor(survivorName, survivorType);
        List<Item> expectedItems = new ArrayList<>();
        expectedItems.add(new Tool(toolName, toolWeight, toolDurability));
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
        Survivor john = survivorService.create(survivorName, survivorType);
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
        Survivor john = survivorService.create(survivorName, survivorType);
        Tool tool = new Tool(toolName, toolWeight, toolDurability);
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
        Survivor john = survivorService.create(survivorName, survivorType);
        Double overloadedToolWeight = 100.0;
        Tool tool = new Tool(toolName, overloadedToolWeight, toolDurability);
        // Act & assert
        assertThrows(OverloadedException.class, () -> survivorService.loadItem(john.getId(), tool));
    }

    @Test
    void loadShouldFailWithHeavyItems() throws Exception {
        // Arrange
        Survivor john = survivorService.create(survivorName, survivorType);
        Tool tool = new Tool(toolName, toolWeight, toolDurability);
        Double overloadedWeaponWeight = 10.0;
        Weapon weapon = new Weapon(weaponName, overloadedWeaponWeight, weaponDamage); // Too heavy
        // Act & assert
        survivorService.loadItem(john.getId(), tool);
        assertThrows(OverloadedException.class, () -> survivorService.loadItem(john.getId(), weapon));
    }

}
