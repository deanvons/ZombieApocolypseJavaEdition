package no.loopacademy.models.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import no.loopacademy.services.ActionService;
import no.loopacademy.services.AuditEntryService;
import no.loopacademy.services.SurvivorService;
import no.loopacademy.services.UserProfileService;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import no.loopacademy.models.attributes.AttributeWeights;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.models.userprofile.UserProfile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ActionTests {
    private static final String KEYCLOAK_ID = "3f2a9c1e-0000-4000-8000-000000000001";

    @InjectMocks
    private SurvivorService survivorService;
    @Mock
    private SurvivorRepository repository;
    private ActionService actionService;
    @Mock
    private ActionRepository actionRepository;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private AuditEntryService auditEntryService;

    private String survivorName;
    private SurvivorType survivorType;
    private Long primaryActionId;

    @BeforeEach
    public void setup() {
        survivorName = "GenericSurvivorName";
        survivorType = SurvivorType.CAREGIVER;
        primaryActionId = 1L;
        Map<Long, Survivor> survivors = new LinkedHashMap<>();
        AtomicLong nextId = new AtomicLong(1);
        actionService = new ActionService(actionRepository);

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

        List<Action> actions = List.of(
                action(1L, "Attack", ActionType.Attack, weights(0.6, 0.2, 0.0, 0.0, 0.1, 0.1, 0.0)),
                action(2L, "Heal", ActionType.Heal, weights(0.0, 0.1, 0.4, 0.4, 0.0, 0.1, 0.0)),
                action(3L, "Scavenge", ActionType.Scavenge, weights(0.1, 0.4, 0.1, 0.3, 0.0, 0.1, 0.0)),
                action(4L, "Build Shelter", ActionType.Build, weights(0.4, 0.1, 0.0, 0.2, 0.1, 0.2, 0.0)),
                action(5L, "Persuade", ActionType.Persuade, weights(0.0, 0.1, 0.4, 0.1, 0.0, 0.0, 0.4)));

        when(actionRepository.findById(1L)).thenReturn(Optional.of(actions.getFirst()));
        when(userProfileService.findByKeycloakId(KEYCLOAK_ID))
                .thenReturn(new UserProfile(KEYCLOAK_ID, "tester"));

    }

    @Test
    void shouldCreateActionWithCorrectValues() {
        // Arr
        String expectedName = "Drug";
        ActionType expectedActionType = ActionType.Disable;
        String expectedEffect = "Poes toe";
        String expectedTarget = "Yo mamma";
        AttributeWeights expectedAttributeWeights = new AttributeWeights();
        expectedAttributeWeights.setStrength(0.1);
        expectedAttributeWeights.setAgility(0.0);
        expectedAttributeWeights.setTrustworthiness(0.6);
        expectedAttributeWeights.setIntelligence(0.1);
        expectedAttributeWeights.setCourage(0.2);
        expectedAttributeWeights.setEndurance(0.0);
        expectedAttributeWeights.setLeadership(0.0);
        // Act
        Action a = new Action(expectedName,
                expectedActionType,
                expectedEffect,
                expectedTarget,
                expectedAttributeWeights);
        String actualName = a.getName();
        ActionType actualActionType = a.getType();
        AttributeWeights actualAttributeWeights = a.getAttributeWeights();
        String actualEffect = a.getEffect();
        String actualTarget = a.getTarget();
        // Ass
        assertEquals(expectedName, actualName);
        assertEquals(expectedActionType, actualActionType);
        assertEquals(expectedEffect, actualEffect);
        assertEquals(expectedTarget, actualTarget);
        assertEquals(expectedAttributeWeights, actualAttributeWeights);
    }

    @Test
    void shouldCalculateCorrectEffectivenessWithoutSkills() {
        double expectedEffectiveness = 29;
        survivorService.create(KEYCLOAK_ID, survivorName, survivorType);
        long survivorId = survivorService.findById(1L).getId();
        long actionId = actionService.findById(primaryActionId).getId(); // Attack type action

        double actualEffectiveness = survivorService.performAction(survivorId, actionId).score();

        assertEquals(expectedEffectiveness, actualEffectiveness);
    }

    private Action action(Long id, String name, ActionType type, AttributeWeights weights) {
        String actionEffect = "";
        String actionTarget = "";
        Action action = new Action(name, type, actionEffect, actionTarget, weights);
        action.setId(id);
        return action;
    }

    private AttributeWeights weights(
            double strength,
            double agility,
            double trustworthiness,
            double intelligence,
            double courage,
            double endurance,
            double leadership) {
        AttributeWeights weights = new AttributeWeights();
        weights.setStrength(strength);
        weights.setAgility(agility);
        weights.setTrustworthiness(trustworthiness);
        weights.setIntelligence(intelligence);
        weights.setCourage(courage);
        weights.setEndurance(endurance);
        weights.setLeadership(leadership);
        return weights;
    }

}
