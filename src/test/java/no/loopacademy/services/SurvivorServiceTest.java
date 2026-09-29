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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;

public class SurvivorServiceTest {
    private SurvivorService survivorService;
    private SurvivorRepository repository;
    private ActionRepository actionRepository;

    @BeforeEach
    public void setup() {
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

        survivorService = new SurvivorService(repository, actionRepository);
    }

    @Test
    public void testSurvivorNotFound_shouldThrowException() {

        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.findById(99L);
        });
    }

    @Test
    void testCreateSurvivorWithCorrectType_checksCaregiverType_shouldPass() {
        Survivor careGiver = new Survivor("Tester", SurvivorType.CAREGIVER);

        Survivor survivor = survivorService.create("Tester2", SurvivorType.CAREGIVER);

        assertEquals(careGiver.getClass(), survivor.getClass());
    }

    @Test
    void testFindAllSurvivors_shouldReturnAllSurvivors_shouldPass() {
        Survivor survivor1 = survivorService.create("Tester1", SurvivorType.CAREGIVER);
        Survivor survivor2 = survivorService.create("Tester2", SurvivorType.CAREGIVER);
        List<Survivor> expectedSurvivors = List.of(survivor1, survivor2);

        List<Survivor> actualsurvivors = survivorService.findAll();

        assertEquals(expectedSurvivors, actualsurvivors);

    }

    @Test
    void testFindSurvivorById_passingSurvivorId_shouldReturnSurvivorById_shouldPass() {
        Survivor expectedSurvivor = survivorService.create("Tester1", SurvivorType.CAREGIVER);
        expectedSurvivor.setId(1L);

        Survivor actualSurvivor = survivorService.findById(1L);

        assertEquals(expectedSurvivor, actualSurvivor);
    }

    @Test
    void testAddSkillToSurvivor_survivor_survivorWithNewSkill() {
        survivorService.create("CareGiver", SurvivorType.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));
        expectedOutput.add(Skill.Accuracy);

        survivorService.addSkill(1L, Skill.Accuracy);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void testAddSkillToSurvivorThatItAlreadyHas_survivorWithSkill_noChangeInSurvivorSkills() {
        survivorService.create("CareGiver", SurvivorType.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));

        survivorService.addSkill(1L, Skill.FieldMedicine);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void testRemoveSkillFromSurvivor_survivorWithSkill_survivorWithoutSkill() {
        survivorService.create("CareGiver", SurvivorType.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(List.of(Skill.FieldMedicine, Skill.Cooking));

        survivorService.removeSkill(1L, Skill.PsychologicalSupport);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void survivorServiceLoadItem_SurvivorIdItem_shouldThrowException() {
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
    void survivorServiceLoadItem_SurvivorIdItem_shouldLoadItemToSurvivor() {
        // ARRANGE
        String survivorName = "Kevin";
        double itemWeight = 5.0;
        Item item = new Item("Medkit", itemWeight);
        List<Item> expextedGearList = new ArrayList<>();
        expextedGearList.add(item);
        Survivor survivor = survivorService.create(survivorName, SurvivorType.CAREGIVER);

        long survivorId = survivor.getId();

        // ACT
        survivorService.loadItem(survivorId, item);
        List<Item> actualGear = survivor.getGear();

        // ASSERT
        assertEquals(expextedGearList, actualGear);
    }

    @Test
    void load_ShouldThrowCarryWeightExceededException_WhenLoadingAnItemThatExceedsMaxWeight() {
        // Arrange
        String expectedName = "Melvin";
        Double heavyItemweight = 9999.0;
        Survivor survivor = survivorService.create(expectedName, SurvivorType.CAREGIVER);
        Item testItem = new Item("Test item", heavyItemweight);

        // Assert
        assertThrows(OverloadedException.class,
                () -> survivorService.loadItem(survivor.getId(), testItem));
    }

}
