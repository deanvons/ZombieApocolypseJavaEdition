package no.loopacademy.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.dtos.response.SurvivorResponse;
import no.loopacademy.mappers.ItemMapper;
import no.loopacademy.mappers.SurvivorMapper;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.services.SurvivorService;

@WebMvcTest(SurvivorController.class)
@AutoConfigureMockMvc(addFilters = false)
class SurvivorControllerTest {

    @Autowired 
    private MockMvc mockMvc;                //sends fake HTTP requests

    @MockitoBean 
    private SurvivorService survivorService; //fake service, w/o db

    @MockitoBean
    private SurvivorMapper survivorMapper;

    @MockitoBean
    private ItemMapper itemMapper;
    
    @Test
    void getSurvivorsShouldReturnListOfSurvivors() throws Exception {
        //Arrange: Setup fake service return value
        Survivor rick = new Survivor("Rick", SurvivorType.OUTLAW);
        when(survivorService.findAll()).thenReturn(List.of(rick));
        when(survivorMapper.toResponse(List.of(rick)))
            .thenReturn(List.of(new SurvivorResponse(null, "Rick", "OUTLAW", rick.getSkills(), List.of())));

        //Act + Assert
        mockMvc.perform(get("/api/survivors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Rick"));
    }

    @Test 
    void getSurvivorsShouldReturnEmptyList() throws Exception {
        //Arrange
        when(survivorService.findAll()).thenReturn(List.of());
        when(survivorMapper.toResponse(List.of())).thenReturn(List.of());

        //Act + assert
        mockMvc.perform(get("/api/survivors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test 
    void getSurvivorByIdShouldReturnSurvivor() throws Exception {
        //Arrange
        Survivor jessica = new Survivor("Jessica", SurvivorType.HERO);
        when(survivorService.findById(1L)).thenReturn(jessica);
        when(survivorMapper.toResponse(jessica))
            .thenReturn(new SurvivorResponse(1L, "Jessica", "HERO", jessica.getSkills(), List.of()));

        //Act + assert
        mockMvc.perform(get("/api/survivors/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Jessica"));
    }

    @Test
    void getSurvivorByIdShouldReturn404IfNotFound() throws Exception {
        //Arrange
        String errorMsg = "Survivor Not Found";
        when(survivorService.findById(1L))
            .thenThrow(new SurvivorNotFoundException(errorMsg));

        //Act + assert
        mockMvc.perform(get("/api/survivors/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(errorMsg));
        
    }

    //Currently only test get methods.
    //TODO: Create tests for POST/PUT and DELETE requests
}
