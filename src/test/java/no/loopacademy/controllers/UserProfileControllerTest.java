package no.loopacademy.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import no.loopacademy.config.authConfig;
import no.loopacademy.dtos.response.UserProfileResponse;
import no.loopacademy.exceptions.ResourceConflictException;
import no.loopacademy.exceptions.UserProfileNotFoundException;
import no.loopacademy.mappers.UserProfileMapper;
import no.loopacademy.models.userprofile.UserProfile;
import no.loopacademy.services.UserProfileService;

@WebMvcTest(UserProfileController.class)
@Import(authConfig.class)
class UserProfileControllerTest {

    private static final String KEYCLOAK_ID = "3f2a9c1e-0000-4000-8000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private UserProfileMapper userProfileMapper;

    private String displayName;
    private Long userProfileId;
    private Instant createdAt;

    @BeforeEach
    void setUp() {
        displayName = "GenericDisplayName";
        userProfileId = 1L;
        createdAt = Instant.parse("2026-01-01T00:00:00Z");
    }

    @Test
    void getMyProfileWithoutTokenShouldReturn401() throws Exception {
        int expectedStatusCode = 401;

        mockMvc.perform(get("/api/profiles/me"))
                .andExpect(status().is(expectedStatusCode));
    }

    @Test
    void getMyProfileShouldLookUpByTokenSubject() throws Exception {
        UserProfile profile = new UserProfile(KEYCLOAK_ID, displayName);
        UserProfileResponse response = new UserProfileResponse(userProfileId, displayName, createdAt, null);
        when(userProfileService.findByKeycloakId(KEYCLOAK_ID)).thenReturn(profile);
        when(userProfileMapper.toResponse(profile)).thenReturn(response);

        mockMvc.perform(get("/api/profiles/me").with(jwt().jwt(jwt -> jwt.subject(KEYCLOAK_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userProfileId))
                .andExpect(jsonPath("$.displayName").value(displayName))
                .andExpect(jsonPath("$.survivorId").isEmpty());

        verify(userProfileService).findByKeycloakId(KEYCLOAK_ID);
    }

    @Test
    void getMyProfileWhenNoneExistsShouldReturn404() throws Exception {
        int expectedStatusCode = 404;
        when(userProfileService.findByKeycloakId(KEYCLOAK_ID))
                .thenThrow(new UserProfileNotFoundException("No profile found for this user"));

        mockMvc.perform(get("/api/profiles/me").with(jwt().jwt(jwt -> jwt.subject(KEYCLOAK_ID))))
                .andExpect(status().is(expectedStatusCode));

        verify(userProfileService).findByKeycloakId(KEYCLOAK_ID);
    }

    @Test
    void createMyProfileShouldUseSubjectAndUsernameFromToken() throws Exception {
        String expectedLocation = "/api/profiles/me";
        UserProfile profile = new UserProfile(KEYCLOAK_ID, displayName);
        UserProfileResponse response = new UserProfileResponse(userProfileId, displayName, createdAt, null);
        when(userProfileService.create(KEYCLOAK_ID, displayName)).thenReturn(profile);
        when(userProfileMapper.toResponse(profile)).thenReturn(response);

        mockMvc.perform(post("/api/profiles/me")
                        .with(jwt().jwt(jwt -> jwt.subject(KEYCLOAK_ID).claim("preferred_username", displayName))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", expectedLocation))
                .andExpect(jsonPath("$.id").value(userProfileId))
                .andExpect(jsonPath("$.displayName").value(displayName));

        verify(userProfileService).create(KEYCLOAK_ID, displayName);
    }

    @Test
    void createMyProfileTwiceShouldReturn409() throws Exception {
        int expectedStatusCode = 409;
        when(userProfileService.create(KEYCLOAK_ID, displayName))
                .thenThrow(new ResourceConflictException("A profile already exists for this user"));

        mockMvc.perform(post("/api/profiles/me")
                        .with(jwt().jwt(jwt -> jwt.subject(KEYCLOAK_ID).claim("preferred_username", displayName))))
                .andExpect(status().is(expectedStatusCode));

        verify(userProfileService).create(KEYCLOAK_ID, displayName);
    }
}
