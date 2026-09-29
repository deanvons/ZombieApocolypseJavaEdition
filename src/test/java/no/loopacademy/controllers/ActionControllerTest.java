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
import no.loopacademy.services.ActionService;

@WebMvcTest(ActionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ActionMapperImpl.class)   // real MapStruct mapper, so the JSON shape is the real one
public class ActionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActionService actionService;

    @Test
    void getActionsShouldReturnListOfActions() throws Exception {
        //Arrange
        Action attack = new Action("Swing axe", ActionType.Attack, "Deals damage", "Zombie", null);
        when(actionService.findAll()).thenReturn(List.of(attack));

        //Act + assert
        mockMvc.perform(get("/api/actions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Swing axe"))
            .andExpect(jsonPath("$[0].type").value("Attack"));
    }

    @Test
    void getActionsShouldReturnEmptyList() throws Exception {
        //Arrange
        when(actionService.findAll()).thenReturn(List.of());

        //Act + assert
        mockMvc.perform(get("/api/actions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getActionByIdShouldReturnAction() throws Exception {
        //Arrange
        Action heal = new Action("Bandage", ActionType.Heal, "Restores health", "Survivor", null);
        heal.setId(1L);
        when(actionService.findById(1L)).thenReturn(heal);

        //Act + assert
        mockMvc.perform(get("/api/actions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Bandage"))
            .andExpect(jsonPath("$.type").value("Heal"));
    }


    @Test
    void getActionByIdShouldThrowNoActionFoundError() throws Exception {
        //Arrange
        String errorMsg = "Action not found";
        when(actionService.findById(1L))
            .thenThrow(new ActionNotFoundException(errorMsg));

        //Act + assert
        mockMvc.perform(get("/api/actions/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(errorMsg));
    }
}
