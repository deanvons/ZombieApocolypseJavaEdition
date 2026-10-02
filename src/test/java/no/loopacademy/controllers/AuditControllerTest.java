package no.loopacademy.controllers;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import no.loopacademy.config.authConfig;
import no.loopacademy.mappers.AuditMapperImpl;
import no.loopacademy.models.audit.AuditActionType;
import no.loopacademy.models.audit.AuditEntry;
import no.loopacademy.models.userprofile.UserProfile;
import no.loopacademy.services.AuditEntryService;

// Security filters stay ON here (unlike SurvivorControllerTest), so these tests also prove
// that the endpoint needs a token (401) and the admin role (403).
@WebMvcTest(AuditController.class)
@Import({ AuditMapperImpl.class, authConfig.class }) // real MapStruct mapper, so the JSON shape is the real one
class AuditControllerTest {
    private static final String KEYCLOAK_ID = "test-user";
    private UserProfile actor;
    private AuditActionType actionType;
    private String expectedActionTypeString;
    private String entityType;
    private Long entityId;
    private String details;

    @Autowired
    private MockMvc mockMvc;                //sends fake HTTP requests

    @MockitoBean
    private AuditEntryService auditEntryService; //fake service, w/o db

    @BeforeEach
    void setup() {
        actor = new UserProfile(KEYCLOAK_ID, "tester");
        actionType = AuditActionType.SKILL_ADDED;
        expectedActionTypeString = actionType.name();
        entityType = "Survivor";
        entityId = 1L;
        details = "Skill Accuracy added to survivor Rick";
    }

    @Test
    void getAllAuditEntries_AsAdmin_ReturnsListOfEntries() throws Exception {
        //Arrange
        int expectedEntryCount = 1;
        AuditEntry entry = new AuditEntry(actor, actionType, entityType, entityId, details);
        when(auditEntryService.findAll()).thenReturn(List.of(entry));

        //Act + assert
        mockMvc.perform(get("/api/audit").with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(expectedEntryCount))
            .andExpect(jsonPath("$[0].actionType").value(expectedActionTypeString))
            .andExpect(jsonPath("$[0].entityType").value(entityType))
            .andExpect(jsonPath("$[0].entityId").value(entityId))
            .andExpect(jsonPath("$[0].details").value(details));

        verify(auditEntryService).findAll();
    }

    @Test
    void getAllAuditEntries_AsAdmin_ReturnsEmptyList() throws Exception {
        //Arrange
        int expectedEntryCount = 0;
        when(auditEntryService.findAll()).thenReturn(List.of());

        //Act + assert
        mockMvc.perform(get("/api/audit").with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(expectedEntryCount));
    }

    @Test
    void getAllAuditEntries_WithoutAdminRole_Returns403() throws Exception {
        mockMvc.perform(get("/api/audit").with(jwt().jwt(j -> j.subject(KEYCLOAK_ID))))
            .andExpect(status().isForbidden());

        verify(auditEntryService, never()).findAll();
    }

    @Test
    void getAllAuditEntries_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/audit"))
            .andExpect(status().isUnauthorized());

        verify(auditEntryService, never()).findAll();
    }

    // jwt() skips the Keycloak role converter, so the authority is set directly
    private RequestPostProcessor adminJwt() {
        return jwt()
            .jwt(j -> j.subject(KEYCLOAK_ID))
            .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
