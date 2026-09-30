package no.loopacademy.controllers;

import no.loopacademy.mappers.ActionMapper;
import no.loopacademy.mappers.ItemMapper;
import no.loopacademy.mappers.SurvivorMapper;
import no.loopacademy.services.ActionService;
import no.loopacademy.services.SurvivorService;
import no.loopacademy.services.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = {
        HealthCheckController.class,
        SurvivorController.class,
        SkillController.class,
        ActionController.class,
})
class ControllerMappingsTest {

    @MockitoBean
    private SurvivorService survivorService;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private SurvivorMapper survivorMapper;

    @MockitoBean
    private ItemMapper itemMapper;

    @MockitoBean 
    private ActionMapper actionMapper;

    @MockitoBean
    private ActionService actionService;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void controllerMappingsStartWithoutConflicts() {
    }
}