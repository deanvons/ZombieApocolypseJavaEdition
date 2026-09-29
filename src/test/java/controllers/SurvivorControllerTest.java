package controllers;


import no.loopacademy.controllers.SurvivorController;
import no.loopacademy.Main;
import no.loopacademy.dtos.request.ItemLoadRequest;
import no.loopacademy.dtos.response.SurvivorResponse;
import no.loopacademy.mappers.ItemMapper;
import no.loopacademy.mappers.SurvivorMapper;
import no.loopacademy.models.actions.ActionResult;
import no.loopacademy.models.items.Tool;
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

import java.util.List;

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

    @MockitoBean
    private SurvivorMapper survivorMapper;

    @MockitoBean
    private ItemMapper itemMapper;

    @Test
    void getSurvivors_ReturnsAllSurvivors() throws Exception {
        Survivor alice = new Survivor("Alice", SurvivorType.CAREGIVER);
        Survivor bob = new Survivor("Bob", SurvivorType.HERO);
        List<Survivor> survivors = List.of(alice, bob);
        List<SurvivorResponse> responses = List.of(
                response(1L, "Alice", "CAREGIVER"),
                response(2L, "Bob", "HERO"));
        when(survivorService.findAll()).thenReturn(survivors);
        when(survivorMapper.toResponse(survivors)).thenReturn(responses);

        mockMvc.perform(get("/api/survivors").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));

        verify(survivorMapper).toResponse(survivors);
    }

    @Test
    void createSurvivor_ReturnsCreatedSurvivor() throws Exception {
        Survivor survivor = new Survivor("Alice", SurvivorType.TESTSURVIVOR);
        survivor.setId(1L);
        SurvivorResponse response = response(1L, "Alice", "TESTSURVIVOR");
        when(survivorMapper.toSurvivorType("testsurvivor")).thenReturn(SurvivorType.TESTSURVIVOR);
        when(survivorService.create("Alice", SurvivorType.TESTSURVIVOR)).thenReturn(survivor);
        when(survivorMapper.toResponse(survivor)).thenReturn(response);

        mockMvc.perform(post("/api/survivors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"type\":\"testsurvivor\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.type").value("TESTSURVIVOR"));

        verify(survivorService).create("Alice", SurvivorType.TESTSURVIVOR);
    }

    @Test
    void getSurvivor_ReturnsSurvivor_WhenFound() throws Exception {
        Survivor survivor = new Survivor("Alice", SurvivorType.TESTSURVIVOR);
        survivor.setId(1L);
        when(survivorService.findById(1L)).thenReturn(survivor);
        when(survivorMapper.toResponse(survivor)).thenReturn(response(1L, "Alice", "TESTSURVIVOR"));

        mockMvc.perform(get("/api/survivors/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void addSkill_ReturnsUpdatedSurvivor() throws Exception {
        Survivor survivor = new Survivor("Alice", SurvivorType.CAREGIVER);
        when(survivorService.findById(1L)).thenReturn(survivor);
        when(survivorMapper.toResponse(survivor)).thenReturn(response(1L, "Alice", "CAREGIVER"));

        mockMvc.perform(post("/api/survivors/1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill\":\"Accuracy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));

        verify(survivorService).addSkill(1L, Skill.Accuracy);
        verify(survivorService).findById(1L);
    }

    @Test
    void deleteSurvivorSkill_RemovesSkillFromSurvivor() throws Exception {
        mockMvc.perform(delete("/api/survivors/1/skills/PsychologicalSupport"))
                .andExpect(status().isNoContent());

        verify(survivorService).removeSkill(1L, Skill.PsychologicalSupport);
    }

    @Test
    void loadItem_ReturnsUpdatedSurvivor() throws Exception {
        Survivor survivor = new Survivor("Alice", SurvivorType.CAREGIVER);
        Tool medkit = new Tool("Medkit", 2.5, 10);
        ItemLoadRequest request = new ItemLoadRequest("tool", "Medkit", 2.5, 10, null);
        when(survivorService.findById(1L)).thenReturn(survivor);
        when(itemMapper.toEntity(request)).thenReturn(medkit);
        when(survivorMapper.toResponse(survivor)).thenReturn(response(1L, "Alice", "CAREGIVER"));

        mockMvc.perform(post("/api/survivors/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"tool\",\"name\":\"Medkit\",\"weight\":2.5,\"durability\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));

        verify(survivorService).loadItem(1L, medkit);
    }

    @Test
    void performAction_ReturnsActionEffectiveness() throws Exception {
        ActionResult result = new ActionResult(null, null, 72.5);
        when(survivorService.performAction(1L, 2L)).thenReturn(result);

        mockMvc.perform(post("/api/survivors/1/actions/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.survivorId").value(1))
                .andExpect(jsonPath("$.actionId").value(2))
                .andExpect(jsonPath("$.effectiveness").value(72.5));

        verify(survivorService).performAction(1L, 2L);
    }

    private SurvivorResponse response(Long id, String name, String type) {
        return new SurvivorResponse(id, name, type, List.of(), List.of());
    }
}

