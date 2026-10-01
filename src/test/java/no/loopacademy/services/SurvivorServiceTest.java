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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import no.loopacademy.repositories.ActionRepository;
import no.loopacademy.repositories.SurvivorRepository;
import no.loopacademy.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.loopacademy.exceptions.ActionNotFoundException;
import no.loopacademy.exceptions.ForbiddenException;
import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.exceptions.ResourceConflictException;
import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.exceptions.UserAlreadyHasSurvivorException;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.models.userprofile.UserProfile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SurvivorServiceTest {
    @InjectMocks
    private SurvivorService survivorService;
    @Mock
    private SurvivorRepository repository;
    @Mock
    private ActionRepository actionRepository;
    @Mock
    private AuditEntryService auditEntryService;
    @Mock
    private UserProfileRepository userProfileRepository;

    private UserProfile actor;

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

        Map<Long, Survivor> survivors = new LinkedHashMap<>();
        AtomicLong nextId = new AtomicLong(1);

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
        actor = newUser();
    }

    @Test
    void findById_SurvivorNotFound_shouldThrowException() {

        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.findById(unknownSurvivorId);
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
    void findByUser_UserHasSurvivor_shouldReturnSurvivor() {
        UserProfile user = newUser();
        Survivor created = survivorService.create(user, survivorName, survivorType);

        assertEquals(created, survivorService.findByUser(user));
    }

    @Test 
    void findByUser_UserHasNoSurvivor_shouldThrowSurvivorNotFoundException() throws Exception {
        UserProfile user = newUser();

        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.findByUser(user);
        });
    }

    @Test
    void create_CaregiverType_shouldReturnSurvivor() {
        Survivor careGiver = new Survivor(survivorName, survivorType);

        Survivor survivor = survivorService.create(newUser(), survivorName, survivorType);

        assertEquals(careGiver.getClass(), survivor.getClass());
    }

    @Test 
    void create_UserAlreadyHasSurvivor_shouldThrowUserAlreadyHasSurvivorException() throws Exception {
        UserProfile user = newUser();
        Survivor firstSurvivor = survivorService.create(user, survivorName, survivorType);

        assertThrows(UserAlreadyHasSurvivorException.class, () -> {
            survivorService.create(user, secondSurvivorName, survivorType);
        });

        assertEquals(firstSurvivor, user.getSurvivor());           // still has the first one
        verify(repository, times(1)).saveAndFlush(any(Survivor.class));
    }

    @Test
    void create_NameAlreadyExists_shouldThrowException() {
        when(repository.existsByName(survivorName)).thenReturn(true);
        
        assertThrows(ResourceConflictException.class, () -> {
            survivorService.create(newUser(), survivorName, survivorType);
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
            survivorService.create(newUser(), survivorName, survivorType);
        });
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
        survivorService.create(actor, survivorName, survivorType);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));
        expectedOutput.add(Skill.Accuracy);

        survivorService.addSkill(actor, survivorId, Skill.Accuracy);
        List<Skill> actualOutput = survivorService.findById(survivorId).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void addSkill_SkillAlreadyKnown_shouldNotChangeSkills() {
        survivorService.create(actor, survivorName, survivorType);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));

        survivorService.addSkill(actor, survivorId, Skill.FieldMedicine);
        List<Skill> actualOutput = survivorService.findById(survivorId).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void removeSkill_ExistingSkill_shouldRemoveSkill() {
        survivorService.create(actor, survivorName, survivorType);
        List<Skill> expectedOutput = new ArrayList<>(List.of(Skill.FieldMedicine, Skill.Cooking));

        survivorService.removeSkill(actor, survivorId, Skill.PsychologicalSupport);
        List<Skill> actualOutput = survivorService.findById(survivorId).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void loadItem_ItemTooHeavy_shouldThrowException() {
        // ARRANGE
        Double overloadedItemWeight = 500000.0;
        survivorService.create(actor, survivorName, survivorType);
        Item item = new Item(itemName, overloadedItemWeight);

        // ACT & ASSERT
        assertThrows(OverloadedException.class, () -> {
            survivorService.loadItem(actor, this.survivorId, item);
        });
    }

    @Test
    void loadItem_ItemWithinCapacity_shouldAddItemToGear() {
        // ARRANGE
        Item item = new Item(itemName, itemWeight);
        List<Item> expectedGearList = new ArrayList<>();
        expectedGearList.add(item);
        Survivor survivor = survivorService.create(actor, survivorName, survivorType);

        // ACT
        survivorService.loadItem(actor, survivor.getId(), item);
        List<Item> actualGear = survivor.getGear();

        // ASSERT
        assertEquals(expectedGearList, actualGear);
    }

    @Test
    void addSkill_UnknownSurvivor_shouldThrowSurvivorNotFoundException() {
        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.addSkill(actor, unknownSurvivorId, Skill.Cooking);
        });
    }

    @Test
    void removeSkill_UnknownSurvivor_shouldThrowSurvivorNotFoundException() {
        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.removeSkill(actor, unknownSurvivorId, Skill.Cooking);
        });
    }

    @Test
    void loadItem_UnknownSurvivor_shouldThrowSurvivorNotFoundException() {
        Item item = new Item(itemName, itemWeight);

        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.loadItem(actor, unknownSurvivorId, item);
        });
    }

    @Test
    void performAction_UnknownSurvivor_shouldThrowSurvivorNotFoundException() {
        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.performAction(actor, unknownSurvivorId, actionId);
        });
    }

    @Test
    void performAction_UnknownAction_shouldThrowActionNotFoundException() {
        Survivor survivor = survivorService.create(actor, survivorName, survivorType);

        assertThrows(ActionNotFoundException.class, () -> {
            survivorService.performAction(actor, survivor.getId(), unknownActionId);
        });
    }

    @Test
    void loadItem_OtherUsersSurvivor_shouldThrowForbiddenException() {
        Survivor survivor = survivorService.create(otherUser(), survivorName, survivorType);
        Item item = new Item(itemName, itemWeight);

        assertThrows(ForbiddenException.class, () ->
                survivorService.loadItem(actor, survivor.getId(), item));

        assertEquals(List.of(), survivor.getGear());
    }

    // A new profile per survivor, so tests that create several survivors don't hit the one-survivor rule
    private UserProfile newUser() {
        return new UserProfile("test-user", "tester");
    }

    private UserProfile otherUser() {
        return new UserProfile("other-test-user", "other tester");
    }
}
