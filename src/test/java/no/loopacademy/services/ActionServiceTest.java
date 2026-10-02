package no.loopacademy.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.loopacademy.exceptions.ActionNotFoundException;
import no.loopacademy.models.actions.Action;
import no.loopacademy.models.actions.ActionType;
import no.loopacademy.models.actions.RequiredItem;
import no.loopacademy.models.attributes.AttributeWeights;
import no.loopacademy.repositories.ActionRepository;

public class ActionServiceTest {
    private ActionService actionService;
    private ActionRepository actionRepository;
    private Long primaryActionId;
    private String primaryActionName;
    private ActionType primaryActionType;

    @BeforeEach
    void setup() {
        primaryActionId = 1L;
        primaryActionName = "Attack";
        primaryActionType = ActionType.Attack;
        actionRepository = mock(ActionRepository.class);

        List<Action> actions = List.of(
                action(primaryActionId, primaryActionName, primaryActionType),
                action(2L, "Heal", ActionType.Heal),
                action(3L, "Forage", ActionType.Forage),
                action(4L, "Build Shelter", ActionType.Build),
                action(5L, "Persuade", ActionType.Persuade));
        actions.getFirst().getRequiredItems().add(new RequiredItem("weapon", null));

        when(actionRepository.findAll()).thenReturn(actions);
        when(actionRepository.findById(primaryActionId)).thenReturn(Optional.of(actions.getFirst()));
        when(actionRepository.findById(999L)).thenReturn(Optional.empty());

        actionService = new ActionService(actionRepository);
    }

    @Test
    void shouldReturnFiveSampleActions() {
        int expectedActionCount = 5;
        assertEquals(expectedActionCount, actionService.findAll().size());
    }

    @Test
    void shouldFindActionById() {
        Action action = actionService.findById(primaryActionId);

        assertEquals(primaryActionName, action.getName());
        assertEquals(primaryActionType, action.getType());
    }

    @Test
    void shouldFindActionWithRequiredItems() {
        Action action = actionService.findById(primaryActionId);

        assertEquals(1, action.getRequiredItems().size());
        assertEquals("weapon", action.getRequiredItems().getFirst().getType());
        assertNull(action.getRequiredItems().getFirst().getName());
    }

    @Test
    void shouldReturnEmptyRequiredItemsForActionsWithoutRequirements() {
        List<Action> actions = actionService.findAll();

        assertTrue(actions.stream().skip(1).allMatch(action -> action.getRequiredItems().isEmpty()));
    }

    @Test
    void shouldThrowForUnknownActionId() {
        assertThrows(ActionNotFoundException.class, () -> actionService.findById(999L));
    }

    private Action action(Long id, String name, ActionType type) {
        String actionEffect = "";
        String actionTarget = "";
        AttributeWeights actionAttributeWeights = new AttributeWeights();
        Action action = new Action(name, type, actionEffect, actionTarget, actionAttributeWeights);
        action.setId(id);
        return action;
    }
}
