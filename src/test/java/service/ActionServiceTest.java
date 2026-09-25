package service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import no.loopacademy.models.actions.Action;
import no.loopacademy.models.actions.ActionType;
import no.loopacademy.services.ActionService;


public class ActionServiceTest{
    @Test
    void shouldReturnFiveSampleActions() {
        ActionService service = new ActionService();

        assertEquals(5, service.findAll().size());
    }

    @Test
    void shouldFindActionById() {
        ActionService service = new ActionService();

        Action action = service.findById(1L);

        assertEquals("Attack", action.getName());
        assertEquals(ActionType.Attack, action.getType());
    }

    @Test
    void shouldReturnNullForUnknownActionId() {
        ActionService service = new ActionService();

        assertNull(service.findById(999L));
    }
}
