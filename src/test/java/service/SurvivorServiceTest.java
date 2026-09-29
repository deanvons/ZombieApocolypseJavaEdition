package service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.loopacademy.exceptions.ActionNotFoundException;
import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.services.SurvivorService;

public class SurvivorServiceTest {
    private SurvivorService survivorService;
    private SurvivorRepository repository;
    private ActionRepository actionRepository;

    @BeforeEach
    void setup() {
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
        when(actionRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        survivorService = new SurvivorService(repository, actionRepository);
    }

    @Test
    void findById_SurvivorNotFound_shouldThrowException() {

        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.findById(99L);
        });
    }

    @Test
    void findById_ExistingId_shouldReturnSurvivor() {
        Survivor expectedSurvivor = survivorService.create("Tester1", SurvivorType.CAREGIVER);
        expectedSurvivor.setId(1L);

        Survivor actualSurvivor = survivorService.findById(1L);

        assertEquals(expectedSurvivor, actualSurvivor);
    }

    @Test
    void create_CaregiverType_shouldReturnSurvivor() {
        Survivor careGiver = new Survivor("Tester", SurvivorType.CAREGIVER);

        Survivor survivor = survivorService.create("Tester2", SurvivorType.CAREGIVER);

        assertEquals(careGiver.getClass(), survivor.getClass());
    }

    @Test
    void findAll_shouldReturnAllSurvivors() {
        Survivor survivor1 = survivorService.create("Tester1", SurvivorType.CAREGIVER);
        Survivor survivor2 = survivorService.create("Tester2", SurvivorType.CAREGIVER);
        List<Survivor> expectedSurvivors = List.of(survivor1, survivor2);

        List<Survivor> actualSurvivors = survivorService.findAll();

        assertEquals(expectedSurvivors, actualSurvivors);

    }

    @Test
    void addSkill_NewSkill_shouldAddSkill() {
        survivorService.create("CareGiver", SurvivorType.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));
        expectedOutput.add(Skill.Accuracy);

        survivorService.addSkill(1L, Skill.Accuracy);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void addSkill_SkillAlreadyKnown_shouldNotChangeSkills() {
        survivorService.create("CareGiver", SurvivorType.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));

        survivorService.addSkill(1L, Skill.FieldMedicine);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void removeSkill_ExistingSkill_shouldRemoveSkill() {
        survivorService.create("CareGiver", SurvivorType.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(List.of(Skill.FieldMedicine, Skill.Cooking));

        survivorService.removeSkill(1L, Skill.PsychologicalSupport);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void loadItem_ItemTooHeavy_shouldThrowException() {
        // ARRANGE
        String survivorName = "Kevin";
        double itemWeight = 500000.0;
        Survivor survivor = survivorService.create(survivorName, SurvivorType.CAREGIVER);
        Item item = new Item("Medkit", itemWeight);
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
        double itemWeight = 5.0;
        Item item = new Item("Medkit", itemWeight);
        List<Item> expectedGearList = new ArrayList<>();
        expectedGearList.add(item);
        Survivor survivor = survivorService.create(survivorName, SurvivorType.CAREGIVER);

        long survivorId = survivor.getId();

        // ACT
        survivorService.loadItem(survivorId, item);
        List<Item> actualGear = survivor.getGear();

        // ASSERT
        assertEquals(expectedGearList, actualGear);
    }

    @Test
    void testAddSkillUnknownSurvivor_shouldThrowSurvivorNotFoundException() {
        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.addSkill(99L, Skill.Cooking);
        });
    }

    @Test
    void testRemoveSkillUnknownSurvivor_shouldThrowSurvivorNotFoundException() {
        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.removeSkill(99L, Skill.Cooking);
        });
    }

    @Test
    void testLoadItemUnknownSurvivor_shouldThrowSurvivorNotFoundException() {
        Item item = new Item("Medkit", 1.0);

        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.loadItem(99L, item);
        });
    }

    @Test
    void testPerformActionUnknownSurvivor_shouldThrowSurvivorNotFoundException() {
        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.performAction(99L, 1L);
        });
    }

    @Test
    void testPerformActionUnknownAction_shouldThrowActionNotFoundException() {
        Survivor survivor = survivorService.create("Tester", SurvivorType.CAREGIVER);

        assertThrows(ActionNotFoundException.class, () -> {
            survivorService.performAction(survivor.getId(), 99L);
        });
    }

}
