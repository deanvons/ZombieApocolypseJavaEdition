package no.loopacademy.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.mappers.ItemMapperImpl;
import no.loopacademy.mappers.SurvivorMapperImpl;
import no.loopacademy.models.actions.ActionResult;
import no.loopacademy.models.attributes.SurvivorAttributes;
import no.loopacademy.models.items.Tool;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.services.SurvivorService;

@WebMvcTest(SurvivorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({SurvivorMapperImpl.class, ItemMapperImpl.class})   // real MapStruct mappers, so the JSON shape is the real one
class SurvivorControllerTest {
    private String survivorName;
    private String expectedSurvivorName;
    private SurvivorType survivorType;
    private String expectedSurvivorTypeString;
    private Long survivorId;
    private Long actionId;

    private String itemMedkit;
    private Double itemMedkitWeight;
    private Double itemMedkitDurability;

    @Autowired 
    private MockMvc mockMvc;                //sends fake HTTP requests

    @MockitoBean 
    private SurvivorService survivorService; //fake service, w/o db


    @BeforeEach
    void setup(){
        survivorName = "Rick";
        expectedSurvivorName = survivorName;
        survivorType = SurvivorType.OUTLAW;
        expectedSurvivorTypeString = survivorType.name();
        survivorId = 1L;
        actionId = 2L;

        itemMedkit = "Medkit";
        itemMedkitWeight = 2.5;
        itemMedkitDurability = 10.0;
    }

    //** GET requests */
    @Test
    void getSurvivorsShouldReturnListOfSurvivors() throws Exception {
        //Arrange: Setup fake service return value
        int expectedSurvivorCount = 1;
        Survivor rick = new Survivor(survivorName, survivorType);
        when(survivorService.findAll()).thenReturn(List.of(rick));

        //Act + Assert
        mockMvc.perform(get("/api/survivors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(expectedSurvivorCount))
            .andExpect(jsonPath("$[0].name").value(expectedSurvivorName))
            .andExpect(jsonPath("$[0].type").value(expectedSurvivorTypeString));
    }

    @Test 
    void getSurvivorsShouldReturnEmptyList() throws Exception {
        //Arrange
        int expectedSurvivorCount = 0;
        when(survivorService.findAll()).thenReturn(List.of());

        //Act + assert
        mockMvc.perform(get("/api/survivors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(expectedSurvivorCount));
    }

    @Test 
    void getSurvivorByIdShouldReturnSurvivor() throws Exception {
        //Arrange
        Survivor jessica = new Survivor(survivorName, survivorType);
        when(survivorService.findById(survivorId)).thenReturn(jessica);

        //Act + assert
        mockMvc.perform(get("/api/survivors/" + survivorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(expectedSurvivorName))
            .andExpect(jsonPath("$.type").value(expectedSurvivorTypeString));
    }

    @Test
    void getSurvivorByIdShouldReturn404IfNotFound() throws Exception {
        //Arrange
        int expectedStatusCode = 404;
        String expectedErrorMessage = "Survivor Not Found";
        when(survivorService.findById(survivorId))
            .thenThrow(new SurvivorNotFoundException(expectedErrorMessage));

        //Act + assert
        mockMvc.perform(get("/api/survivors/" + survivorId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(expectedStatusCode))
            .andExpect(jsonPath("$.message").value(expectedErrorMessage));
        
    }

    @Test
    void getSurvivorGear_ReturnsGearItems() throws Exception {
        Tool expectedGearItem = new Tool(itemMedkit, itemMedkitWeight, itemMedkitDurability);
        Survivor survivor = new Survivor(survivorName, survivorType);
        survivor.setGear(List.of(expectedGearItem));
        when(survivorService.findById(survivorId)).thenReturn(survivor);

        mockMvc.perform(get("/api/survivors/" + survivorId + "/gear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("tool"))
            .andExpect(jsonPath("$[0].name").value(expectedGearItem.getName()))
            .andExpect(jsonPath("$[0].weight").value(expectedGearItem.getWeight()))
            .andExpect(jsonPath("$[0].durability").value(expectedGearItem.getDurability()));

        verify(survivorService).findById(survivorId);
    }

    @Test
    void getSurvivorAttributes_ReturnsAttributes() throws Exception {
        SurvivorAttributes expectedAttributes = new SurvivorAttributes();
        expectedAttributes.setStrength(7.0);
        Survivor survivor = new Survivor(survivorName, survivorType);
        survivor.setAttributes(expectedAttributes);
        when(survivorService.findById(survivorId)).thenReturn(survivor);

        mockMvc.perform(get("/api/survivors/" + survivorId + "/attributes"))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.strength").value(expectedAttributes.getStrength()));

        verify(survivorService).findById(survivorId);
    }

    @Test
    void getSurvivorSkills_ReturnsSkills() throws Exception {
        List<Skill> expectedSkills = List.of(Skill.Accuracy, Skill.PsychologicalSupport);
        Survivor survivor = new Survivor(survivorName, survivorType);
        survivor.setSkills(expectedSkills);
        when(survivorService.findById(survivorId)).thenReturn(survivor);

        mockMvc.perform(get("/api/survivors/" + survivorId + "/skills"))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(expectedSkills.size()))
            .andExpect(jsonPath("$[0]").value(expectedSkills.get(0).name()))
            .andExpect(jsonPath("$[1]").value(expectedSkills.get(1).name()));

        verify(survivorService).findById(survivorId);
    }

    /** POST, PUT, DELETE requests */
    @Test
    void createSurvivor_ReturnsCreatedSurvivor() throws Exception {
        Survivor survivor = new Survivor(survivorName, survivorType);
        survivor.setId(survivorId);
        // SurvivorResponse response = response(expectedSurvivorId, survivorName, expectedSurvivorType);
        when(survivorService.create(survivorName, survivorType)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + survivorName
                                + "\",\"type\":\"" + expectedSurvivorTypeString + "\"}"))
                .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(survivorId))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(survivorName))
            .andExpect(jsonPath("$.type").value(expectedSurvivorTypeString));

        verify(survivorService).create(expectedSurvivorName, survivorType);
    }

    @Test
    void addSkill_ReturnsUpdatedSurvivor() throws Exception {
        Survivor survivor = new Survivor(survivorName, survivorType);
        when(survivorService.findById(survivorId)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors/" + survivorId + "/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill\":\"Accuracy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(expectedSurvivorName));

        verify(survivorService).addSkill(survivorId, Skill.Accuracy);
        verify(survivorService).findById(survivorId);
    }

    @Test
    void deleteSurvivorSkill_RemovesSkillFromSurvivor() throws Exception {
        mockMvc.perform(delete("/api/survivors/" + survivorId + "/skills/PsychologicalSupport"))
                .andExpect(status().isNoContent());

        verify(survivorService).removeSkill(survivorId, Skill.PsychologicalSupport);
    }

    @Test
    void loadItem_ReturnsUpdatedSurvivor() throws Exception {

        Survivor survivor = new Survivor(survivorName, survivorType);

        Tool medkit = new Tool(itemMedkit, itemMedkitWeight, itemMedkitDurability);
        when(survivorService.findById(survivorId)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors/" + survivorId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"tool\",\"name\":\"" + itemMedkit
                                + "\",\"weight\":" + itemMedkitWeight
                                + ",\"durability\":" + itemMedkitDurability + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(expectedSurvivorName));

        verify(survivorService).loadItem(survivorId, medkit);
    }

    @Test
    void performAction_ReturnsActionEffectiveness() throws Exception {
        double expectedEffectiveness = 72.5;
        ActionResult result = new ActionResult(null, null, expectedEffectiveness);
        when(survivorService.performAction(survivorId, actionId)).thenReturn(result);

        mockMvc.perform(post("/api/survivors/" + survivorId + "/actions/" + actionId))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.survivorId").value(survivorId))
            .andExpect(jsonPath("$.actionId").value(actionId))
            .andExpect(jsonPath("$.effectiveness").value(expectedEffectiveness));

        verify(survivorService).performAction(survivorId, actionId);
    }

}
