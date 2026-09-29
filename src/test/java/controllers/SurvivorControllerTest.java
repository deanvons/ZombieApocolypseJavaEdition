package controllers;


import no.loopacademy.controllers.SurvivorController;
import no.loopacademy.Main;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.services.SurvivorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(SurvivorController.class)
@ContextConfiguration(classes = Main.class)
@AutoConfigureMockMvc(addFilters = false)
public class SurvivorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SurvivorService survivorService;

    @Test
    void getSurvivors_ReturnsAllSurvivors() throws Exception {
        Survivor alice = new Survivor("Alice", SurvivorType.CAREGIVER);
        alice.setId(1L);
        Survivor bob = new Survivor("Bob", SurvivorType.HERO);
        bob.setId(2L);
        when(survivorService.findAll()).thenReturn(List.of(alice, bob));

        mockMvc.perform(get("/api/survivors").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void createSurvivor_ReturnsCreatedSurvivor() throws Exception {
        Survivor survivor = new Survivor("Alice", SurvivorType.TESTSURVIVOR);
        survivor.setId(1L);
        when(survivorService.create("Alice", SurvivorType.TESTSURVIVOR)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"type\":\"TESTSURVIVOR\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"));

        verify(survivorService).create("Alice", SurvivorType.TESTSURVIVOR);
    }

    @Test
    void getSurvivor_ReturnsSurvivor_WhenFound() throws Exception {
        Survivor survivor = new Survivor("Alice", SurvivorType.TESTSURVIVOR);
        survivor.setId(1L);
        when(survivorService.findById(1L)).thenReturn(survivor);

        mockMvc.perform(get("/api/survivors/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void setSurvivorSkills_AddsSkillsToSurvivor() throws Exception {
        Survivor survivor = new Survivor("Alice", SurvivorType.CAREGIVER);
        when(survivorService.findById(1L)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors/1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"Accuracy\"]"))
                .andExpect(status().isOk());

        assertTrue(survivor.getSkills().contains(Skill.Accuracy));
        assertTrue(survivor.getSkills().contains(Skill.FieldMedicine));
    }

    @Test
    void deleteSurvivorSkill_RemovesSkillFromSurvivor() throws Exception {
        Survivor survivor = new Survivor("Alice", SurvivorType.CAREGIVER);
        when(survivorService.findById(1L)).thenReturn(survivor);

        mockMvc.perform(delete("/api/survivors/1/skills/PsychologicalSupport"))
                .andExpect(status().isOk());

        assertFalse(survivor.getSkills().contains(Skill.PsychologicalSupport));
    }

    @Test
    void setSurvivorItems_AddsItemsToSurvivor() throws Exception {
        Survivor survivor = new Survivor("Alice", SurvivorType.CAREGIVER);
        survivor.setGear(new ArrayList<>(List.of(new Item("Torch", 1.0))));
        when(survivorService.findById(1L)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"name\":\"Medkit\",\"weight\":2.5}]"))
                .andExpect(status().isOk());

        assertEquals(List.of(new Item("Medkit", 2.5), new Item("Torch", 1.0)), survivor.getGear());
    }

    @Test
    void performAction_CallsPerformActionForSurvivor() throws Exception {
        Survivor survivor = mock(Survivor.class);
        when(survivorService.findById(1L)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors/1/2"))
                .andExpect(status().isOk());

        verify(survivor).performAction(null);
    }
}

