package no.loopacademy.controllers;

import no.loopacademy.config.authConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest doesn't load @Configuration classes, so without this import the test
// runs with Spring Boot's default security (everything locked, 401) instead of ours.
@WebMvcTest(HealthCheckController.class)
@Import(authConfig.class)
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void getApiHealthShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void databaseCheckAliveQuerySucceeds() throws Exception{
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        mockMvc.perform(get("/api/health/database"))
                .andExpect(status().isOk());
    }

    @Test
    void databaseCheckAliveQueryFails() throws Exception{
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
        .thenThrow(new DataAccessResourceFailureException("Database unavailable"));
        mockMvc.perform(get("/api/health/database"))
                .andExpect(status().isServiceUnavailable());
    }
}
