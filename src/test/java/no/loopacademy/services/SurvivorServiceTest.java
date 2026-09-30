package no.loopacademy.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.models.userprofile.UserProfile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SurvivorServiceTest {
    private static final String KEYCLOAK_ID = "3f2a9c1e-0000-4000-8000-000000000001";

    @InjectMocks
    private SurvivorService survivorService;
    @Mock
    private SurvivorRepository repository;
    @Mock
    private ActionRepository actionRepository;
    @Mock
    private AuditEntryService auditEntryService;
    @Mock
    private UserProfileService userProfileService;

    @BeforeEach
    void setup() {
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
        when(repository.findAll()).thenAnswer(invocation -> new ArrayList<>(survivors.values()));
        when(repository.findById(any(Long.class)))
                .thenAnswer(invocation -> Optional.ofNullable(survivors.get(invocation.getArgument(0))));
        when(repository.getReferenceById(any(Long.class)))
                .thenAnswer(invocation -> survivors.get(invocation.getArgument(0)));
        when(userProfileService.findByKeycloakId(KEYCLOAK_ID))
                .thenReturn(new UserProfile(KEYCLOAK_ID, "tester"));
    }

    @Test
    void findById_SurvivorNotFound_shouldThrowException() {

        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.findById(99L);
        });
    }

    @Test
    void findById_ExistingId_shouldReturnSurvivor() {
        Survivor expectedSurvivor = survivorService.create(KEYCLOAK_ID, "Tester1", SurvivorType.CAREGIVER);
        expectedSurvivor.setId(1L);

        Survivor actualSurvivor = survivorService.findById(1L);

        assertEquals(expectedSurvivor, actualSurvivor);
    }

    @Test
    void create_CaregiverType_shouldReturnSurvivor() {
        String survivorName = "Tester";
        SurvivorType survivorType = SurvivorType.CAREGIVER;
        Survivor careGiver = new Survivor(survivorName, survivorType);

        Survivor survivor = survivorService.create(KEYCLOAK_ID, "Tester2", SurvivorType.CAREGIVER);

        assertEquals(careGiver.getClass(), survivor.getClass());
    }

    @Test
    void findAll_shouldReturnAllSurvivors() {
        Survivor survivor1 = survivorService.create(KEYCLOAK_ID, "Tester1", SurvivorType.CAREGIVER);
        Survivor survivor2 = survivorService.create(KEYCLOAK_ID, "Tester2", SurvivorType.CAREGIVER);
        List<Survivor> expectedSurvivors = List.of(survivor1, survivor2);

        List<Survivor> actualSurvivors = survivorService.findAll();

        assertEquals(expectedSurvivors, actualSurvivors);

    }

    @Test
    void addSkill_NewSkill_shouldAddSkill() {
        survivorService.create(KEYCLOAK_ID, "CareGiver", SurvivorType.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));
        expectedOutput.add(Skill.Accuracy);

        survivorService.addSkill(1L, Skill.Accuracy);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void addSkill_SkillAlreadyKnown_shouldNotChangeSkills() {
        survivorService.create(KEYCLOAK_ID, "CareGiver", SurvivorType.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));

        survivorService.addSkill(1L, Skill.FieldMedicine);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void removeSkill_ExistingSkill_shouldRemoveSkill() {
        survivorService.create(KEYCLOAK_ID, "CareGiver", SurvivorType.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(List.of(Skill.FieldMedicine, Skill.Cooking));

        survivorService.removeSkill(1L, Skill.PsychologicalSupport);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void loadItem_ItemTooHeavy_shouldThrowException() {
        // ARRANGE
        String survivorName = "Kevin";
        SurvivorType survivorType = SurvivorType.CAREGIVER;
        String itemName = "Medkit";
        Double itemWeight = 500000.0;
        Survivor survivor = survivorService.create(KEYCLOAK_ID, survivorName, survivorType);
        Item item = new Item(itemName, itemWeight);
        long survivorId = survivor.getId();

        // ACT & ASSERT
        assertThrows(OverloadedException.class, () -> {
            survivorService.loadItem(survivorId, item);
        });
    }

    @Test
    void loadItem_ItemWithinCapacity_shouldAddItemToGear() {
        // ARRANGE
        String survivorName = "Kevin";
        SurvivorType survivorType = SurvivorType.CAREGIVER;
        String itemName = "Medkit";
        Double itemWeight = 5.0;
        Item item = new Item(itemName, itemWeight);
        List<Item> expectedGearList = new ArrayList<>();
        expectedGearList.add(item);
        Survivor survivor = survivorService.create(KEYCLOAK_ID, survivorName, survivorType);

        long survivorId = survivor.getId();

        // ACT
        survivorService.loadItem(survivorId, item);
        List<Item> actualGear = survivor.getGear();

        // ASSERT
        assertEquals(expectedGearList, actualGear);
    }

}
