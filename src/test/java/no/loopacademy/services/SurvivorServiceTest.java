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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import no.loopacademy.repositories.ActionRepository;
import no.loopacademy.repositories.SurvivorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import no.loopacademy.exceptions.ActionNotFoundException;
import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.exceptions.ResourceConflictException;
import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;

public class SurvivorServiceTest {
    private SurvivorService survivorService;
    private SurvivorRepository repository;
    private ActionRepository actionRepository;
    private String survivorName;
    private String secondSurvivorName;
    private SurvivorType survivorType;
    private Long survivorId;
    private Long unknownSurvivorId;
    private Long actionId;
    private Long unknownActionId;
    private String itemName;
    private Double itemWeight;

    @BeforeEach
    void setup() {
        survivorName = "GenericSurvivorName";
        secondSurvivorName = "SecondGenericSurvivorName";
        survivorType = SurvivorType.CAREGIVER;
        survivorId = 1L;
        unknownSurvivorId = 99L;
        actionId = 1L;
        unknownActionId = 99L;
        itemName = "GenericItemName";
        itemWeight = 5.0;

        repository = mock(SurvivorRepository.class);
        Map<Long, Survivor> survivors = new LinkedHashMap<>();
        AtomicLong nextId = new AtomicLong(1);
        actionRepository = mock(ActionRepository.class);

        when(repository.saveAndFlush(any(Survivor.class))).thenAnswer(invocation -> {
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
        when(actionRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        survivorService = new SurvivorService(repository, actionRepository);
    }

    @Test
    void findById_SurvivorNotFound_shouldThrowException() {

        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.findById(unknownSurvivorId);
        });
    }

    @Test
    void findById_ExistingId_shouldReturnSurvivor() {
        Survivor expectedSurvivor = survivorService.create(survivorName, survivorType);
        expectedSurvivor.setId(survivorId);

        Survivor actualSurvivor = survivorService.findById(survivorId);

        assertEquals(expectedSurvivor, actualSurvivor);
    }

    @Test
    void create_CaregiverType_shouldReturnSurvivor() {
        Survivor careGiver = new Survivor(survivorName, survivorType);

        Survivor survivor = survivorService.create(survivorName, survivorType);

        assertEquals(careGiver.getClass(), survivor.getClass());
    }

    @Test
    void create_NameAlreadyExists_shouldThrowException() {
        when(repository.existsByName(survivorName)).thenReturn(true);
        
        assertThrows(ResourceConflictException.class, () -> {
            survivorService.create(survivorName, survivorType);
        });
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void create_SimultaneousRequests_shouldThrowException() {
        // Simulates two simultaneous first requests: the check passes, the unique constraint catches it
        when(repository.existsByName(survivorName)).thenReturn(false);
        when(repository.saveAndFlush(any(Survivor.class)))
            .thenThrow(new DataIntegrityViolationException(""));

        assertThrows(ResourceConflictException.class, () -> {
            survivorService.create(survivorName, survivorType);
        });
    }

    @Test
    void findAll_shouldReturnAllSurvivors() {
        Survivor survivor1 = survivorService.create(survivorName, survivorType);
        Survivor survivor2 = survivorService.create(secondSurvivorName, survivorType);
        List<Survivor> expectedSurvivors = List.of(survivor1, survivor2);

        List<Survivor> actualSurvivors = survivorService.findAll();

        assertEquals(expectedSurvivors, actualSurvivors);

    }

    @Test
    void addSkill_NewSkill_shouldAddSkill() {
        survivorService.create(survivorName, survivorType);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));
        expectedOutput.add(Skill.Accuracy);

        survivorService.addSkill(survivorId, Skill.Accuracy);
        List<Skill> actualOutput = survivorService.findById(survivorId).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void addSkill_SkillAlreadyKnown_shouldNotChangeSkills() {
        survivorService.create(survivorName, survivorType);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));

        survivorService.addSkill(survivorId, Skill.FieldMedicine);
        List<Skill> actualOutput = survivorService.findById(survivorId).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void removeSkill_ExistingSkill_shouldRemoveSkill() {
        survivorService.create(survivorName, survivorType);
        List<Skill> expectedOutput = new ArrayList<>(List.of(Skill.FieldMedicine, Skill.Cooking));

        survivorService.removeSkill(survivorId, Skill.PsychologicalSupport);
        List<Skill> actualOutput = survivorService.findById(survivorId).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void loadItem_ItemTooHeavy_shouldThrowException() {
        // ARRANGE
        Double overloadedItemWeight = 500000.0;
        survivorService.create(survivorName, survivorType);
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
        Survivor survivor = survivorService.create(survivorName, survivorType);

        // ACT
        survivorService.loadItem(survivor.getId(), item);
        List<Item> actualGear = survivor.getGear();

        // ASSERT
        assertEquals(expectedGearList, actualGear);
    }

    @Test
    void addSkill_UnknownSurvivor_shouldThrowSurvivorNotFoundException() {
        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.addSkill(unknownSurvivorId, Skill.Cooking);
        });
    }

    @Test
    void removeSkill_UnknownSurvivor_shouldThrowSurvivorNotFoundException() {
        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.removeSkill(unknownSurvivorId, Skill.Cooking);
        });
    }

    @Test
    void loadItem_UnknownSurvivor_shouldThrowSurvivorNotFoundException() {
        Item item = new Item(itemName, itemWeight);

        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.loadItem(unknownSurvivorId, item);
        });
    }

    @Test
    void performAction_UnknownSurvivor_shouldThrowSurvivorNotFoundException() {
        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.performAction(unknownSurvivorId, actionId);
        });
    }

    @Test
    void performAction_UnknownAction_shouldThrowActionNotFoundException() {
        Survivor survivor = survivorService.create(survivorName, survivorType);

        assertThrows(ActionNotFoundException.class, () -> {
            survivorService.performAction(survivor.getId(), unknownActionId);
        });
    }

}
