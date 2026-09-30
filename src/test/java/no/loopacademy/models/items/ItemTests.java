package no.loopacademy.models.items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import no.loopacademy.repositories.ActionRepository;
import no.loopacademy.repositories.SurvivorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.models.userprofile.UserProfile;
import no.loopacademy.services.AuditEntryService;
import no.loopacademy.services.SurvivorService;
import no.loopacademy.services.UserProfileService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ItemTests {
    private static final String KEYCLOAK_ID = "3f2a9c1e-0000-4000-8000-000000000001";

    @InjectMocks
    private SurvivorService survivorService;
    @Mock
    private ActionRepository actionRepository;
    @Mock
    private SurvivorRepository repository;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private AuditEntryService auditEntryService;

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

        Map<Long, Survivor> survivors = new LinkedHashMap<>();
        AtomicLong nextId = new AtomicLong(1);

        when(repository.save(any(Survivor.class))).thenAnswer(invocation -> {
            Survivor survivor = invocation.getArgument(0);
            if (survivor.getId() == null) {
                survivor.setId(nextId.getAndIncrement());
            }
            survivors.put(survivor.getId(), survivor);
            return survivor;
        });
        when(userProfileService.findByKeycloakId(KEYCLOAK_ID))
                .thenReturn(new UserProfile(KEYCLOAK_ID, "tester"));
        when(repository.findById(any(Long.class)))
                .thenAnswer(invocation -> java.util.Optional.ofNullable(survivors.get(invocation.getArgument(0))));

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
        Survivor john = survivorService.create(KEYCLOAK_ID, survivorName, survivorType);
        Tool tool = new Tool(toolName, toolWeight, toolDurability);
        int expectedGearItemCount = 1;
        // Act
        survivorService.loadItem(KEYCLOAK_ID, john.getId(), tool);
        // Assert - to check if he has it equipped
        assertEquals(expectedGearItemCount, john.getGear().size());
    }

    @Test
    void shouldBeAbleToLoadItemsIfUnderWeightLimit() throws Exception {
        // Arrange
        Survivor john = survivorService.create(KEYCLOAK_ID, survivorName, survivorType);
        Tool tool = new Tool(toolName, toolWeight, toolDurability);
        Weapon weapon = new Weapon(weaponName, weaponWeight, weaponDamage);
        int expectedGearItemCount = 2;
        // Act
        survivorService.loadItem(KEYCLOAK_ID, john.getId(), tool);
        survivorService.loadItem(KEYCLOAK_ID, john.getId(), weapon);
        // Assert - to check if he has it equipped
        assertEquals(expectedGearItemCount, john.getGear().size());
    }

    @Test
    void loadShouldFailWithHeavyItem() throws Exception {
        // Arrange
        Survivor john = survivorService.create(KEYCLOAK_ID, survivorName, survivorType);
        Double overloadedToolWeight = 100.0;
        Tool tool = new Tool(toolName, overloadedToolWeight, toolDurability);
        // Act & assert
        assertThrows(OverloadedException.class, () -> survivorService.loadItem(KEYCLOAK_ID, john.getId(), tool));
    }

    @Test
    void loadShouldFailWithHeavyItems() throws Exception {
        // Arrange
        Survivor john = survivorService.create(KEYCLOAK_ID, survivorName, survivorType);
        Tool tool = new Tool(toolName, toolWeight, toolDurability);
        Double overloadedWeaponWeight = 10.0;
        Weapon weapon = new Weapon(weaponName, overloadedWeaponWeight, weaponDamage); // Too heavy
        // Act & assert
        survivorService.loadItem(KEYCLOAK_ID, john.getId(), tool);
        assertThrows(OverloadedException.class, () -> survivorService.loadItem(KEYCLOAK_ID, john.getId(), weapon));
    }

}
