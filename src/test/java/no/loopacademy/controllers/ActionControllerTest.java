package no.loopacademy.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import no.loopacademy.exceptions.ActionNotFoundException;
import no.loopacademy.mappers.ActionMapperImpl;
import no.loopacademy.models.actions.Action;
import no.loopacademy.models.actions.ActionType;
import no.loopacademy.models.attributes.AttributeWeights;
import no.loopacademy.services.ActionService;

@WebMvcTest(ActionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ActionMapperImpl.class)   // real MapStruct mapper, so the JSON shape is the real one
class ActionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActionService actionService;

    @Test
    void getActionsShouldReturnListOfActions() throws Exception {
        //Arrange
        String actionName = "Swing axe";
        String expectedActionName = "Swing axe";
        String expectedActionType = "Attack";
        int expectedActionCount = 1;
        ActionType actionType = ActionType.Attack;
        String actionEffect = "Deals damage";
        String actionTarget = "Zombie";
        AttributeWeights actionAttributeWeights = null;
        Action attack = new Action(actionName, actionType, actionEffect, actionTarget, actionAttributeWeights);
        when(actionService.findAll()).thenReturn(List.of(attack));

        //Act + assert
        mockMvc.perform(get("/api/actions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(expectedActionCount))
            .andExpect(jsonPath("$[0].name").value(expectedActionName))
            .andExpect(jsonPath("$[0].type").value(expectedActionType));
    }

    @Test
    void getActionsShouldReturnEmptyList() throws Exception {
        //Arrange
        int expectedActionCount = 0;
        when(actionService.findAll()).thenReturn(List.of());

        //Act + assert
        mockMvc.perform(get("/api/actions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(expectedActionCount));
    }

    @Test
    void getActionByIdShouldReturnAction() throws Exception {
        //Arrange
        String actionName = "Bandage";
        Long expectedActionId = 1L;
        String expectedActionName = "Bandage";
        String expectedActionType = "Heal";
        ActionType actionType = ActionType.Heal;
        String actionEffect = "Restores health";
        String actionTarget = "Survivor";
        AttributeWeights actionAttributeWeights = null;
        Action heal = new Action(actionName, actionType, actionEffect, actionTarget, actionAttributeWeights);
        heal.setId(expectedActionId);
        when(actionService.findById(expectedActionId)).thenReturn(heal);

        //Act + assert
        mockMvc.perform(get("/api/actions/" + expectedActionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedActionId))
            .andExpect(jsonPath("$.name").value(expectedActionName))
            .andExpect(jsonPath("$.type").value(expectedActionType));
    }


    @Test
    void getActionByIdShouldThrowNoActionFoundError() throws Exception {
        //Arrange
        Long actionId = 1L;
        int expectedStatusCode = 404;
        String expectedErrorMessage = "Action not found";
        when(actionService.findById(actionId))
            .thenThrow(new ActionNotFoundException(expectedErrorMessage));

        //Act + assert
        mockMvc.perform(get("/api/actions/" + actionId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(expectedStatusCode))
            .andExpect(jsonPath("$.message").value(expectedErrorMessage));
    }
}
