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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.loopacademy.repositories.ActionRepository;
import no.loopacademy.repositories.SurvivorRepository;
import no.loopacademy.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.models.userprofile.UserProfile;

public class SurvivorServiceTest {
    private SurvivorService survivorService;
    private UserProfileRepository userProfileRepository;
    private SurvivorRepository repository;
    private ActionRepository actionRepository;
    private String survivorName;
    private String secondSurvivorName;
    private SurvivorType survivorType;
    private Long survivorId;
    private String itemName;
    private Double itemWeight;

    @BeforeEach
    void setup() {
        survivorName = "GenericSurvivorName";
        secondSurvivorName = "SecondGenericSurvivorName";
        survivorType = SurvivorType.CAREGIVER;
        survivorId = 1L;
        itemName = "GenericItemName";
        itemWeight = 5.0;

        repository = mock(SurvivorRepository.class);
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
        when(repository.findAll()).thenAnswer(invocation -> new ArrayList<>(survivors.values()));
        when(repository.findById(any(Long.class)))
                .thenAnswer(invocation -> Optional.ofNullable(survivors.get(invocation.getArgument(0))));
        when(repository.getReferenceById(any(Long.class)))
                .thenAnswer(invocation -> survivors.get(invocation.getArgument(0)));

        userProfileRepository = mock(UserProfileRepository.class);

        survivorService = new SurvivorService(repository, actionRepository, userProfileRepository);
    }

    @Test
    void findById_SurvivorNotFound_shouldThrowException() {

        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.findById(99L);
        });
    }

    @Test
    void findById_ExistingId_shouldReturnSurvivor() {
        Survivor expectedSurvivor = survivorService.create(newUser(), survivorName, survivorType);
        expectedSurvivor.setId(survivorId);

        Survivor actualSurvivor = survivorService.findById(survivorId);

        assertEquals(expectedSurvivor, actualSurvivor);
    }

    @Test
    void create_CaregiverType_shouldReturnSurvivor() {
        Survivor careGiver = new Survivor(survivorName, survivorType);

        Survivor survivor = survivorService.create(newUser(), survivorName, survivorType);

        assertEquals(careGiver.getClass(), survivor.getClass());
    }

    @Test
    void findAll_shouldReturnAllSurvivors() {
        Survivor survivor1 = survivorService.create(newUser(), survivorName, survivorType);
        Survivor survivor2 = survivorService.create(newUser(), secondSurvivorName, survivorType);
        List<Survivor> expectedSurvivors = List.of(survivor1, survivor2);

        List<Survivor> actualSurvivors = survivorService.findAll();

        assertEquals(expectedSurvivors, actualSurvivors);

    }

    @Test
    void addSkill_NewSkill_shouldAddSkill() {
        survivorService.create(newUser(), survivorName, survivorType);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));
        expectedOutput.add(Skill.Accuracy);

        survivorService.addSkill(survivorId, Skill.Accuracy);
        List<Skill> actualOutput = survivorService.findById(survivorId).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void addSkill_SkillAlreadyKnown_shouldNotChangeSkills() {
        survivorService.create(newUser(), survivorName, survivorType);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));

        survivorService.addSkill(survivorId, Skill.FieldMedicine);
        List<Skill> actualOutput = survivorService.findById(survivorId).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void removeSkill_ExistingSkill_shouldRemoveSkill() {
        survivorService.create(newUser(), survivorName, survivorType);
        List<Skill> expectedOutput = new ArrayList<>(List.of(Skill.FieldMedicine, Skill.Cooking));

        survivorService.removeSkill(survivorId, Skill.PsychologicalSupport);
        List<Skill> actualOutput = survivorService.findById(survivorId).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void loadItem_ItemTooHeavy_shouldThrowException() {
        // ARRANGE
        Double overloadedItemWeight = 500000.0;
        survivorService.create(newUser(), survivorName, survivorType);
        Item item = new Item(itemName, overloadedItemWeight);

        // ACT & ASSERT
        assertThrows(OverloadedException.class, () -> {
            survivorService.loadItem(this.survivorId, item);
        });
    }

    @Test
    void loadItem_ItemWithinCapacity_shouldAddItemToGear() {
        // ARRANGE
        Item item = new Item(itemName, itemWeight);
        List<Item> expectedGearList = new ArrayList<>();
        expectedGearList.add(item);
        Survivor survivor = survivorService.create(newUser(), survivorName, survivorType);

        // ACT
        survivorService.loadItem(survivor.getId(), item);
        List<Item> actualGear = survivor.getGear();

        // ASSERT
        assertEquals(expectedGearList, actualGear);
    }

    // A new profile per survivor, so tests that create several survivors don't hit the one-survivor rule
    private UserProfile newUser() {
        return new UserProfile("test-user", "tester");
    }
}
