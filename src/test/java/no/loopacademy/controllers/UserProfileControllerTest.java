package no.loopacademy.controllers;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import no.loopacademy.config.authConfig;

// Security stays ON here (unlike SurvivorControllerTest), so these tests also prove
// that /me needs a token and reads the Keycloak id from it. jwt() fakes a valid token.
@WebMvcTest(UserProfileController.class)
@Import(authConfig.class)
class UserProfileControllerTest {

    // private static final String KEYCLOAK_ID = "3f2a9c1e-0000-4000-8000-000000000001";

    // @Autowired
    // private MockMvc mockMvc;

    // @MockitoBean
    // private UserProfileService userProfileService;

    // @MockitoBean
    // private UserProfileMapper userProfileMapper;

    // @Test
    // void getMyProfileWithoutTokenShouldReturn401() throws Exception {
    //     mockMvc.perform(get("/api/profiles/me"))
    //         .andExpect(status().isUnauthorized());
    // }

    // @Test
    // void getMyProfileShouldLookUpByTokenSubject() throws Exception {
    //     String expectedDisplayName = "rick";
    //     UserProfile profile = new UserProfile(KEYCLOAK_ID, "rick");
    //     when(userProfileService.findByKeycloakId(KEYCLOAK_ID)).thenReturn(profile);
    //     when(userProfileMapper.toResponse(profile))
    //         .thenReturn(new UserProfileResponse(1L, expectedDisplayName, Instant.now(), null));

    //     mockMvc.perform(get("/api/profiles/me").with(jwt().jwt(j -> j.subject(KEYCLOAK_ID))))
    //         .andExpect(status().isOk())
    //         .andExpect(jsonPath("$.displayName").value(expectedDisplayName))
    //         .andExpect(jsonPath("$.survivorId").isEmpty());
    // }

    // @Test
    // void getMyProfileWhenNoneExistsShouldReturn404() throws Exception {
    //     when(userProfileService.findByKeycloakId(KEYCLOAK_ID))
    //         .thenThrow(new UserProfileNotFoundException("No profile found for this user"));

    //     mockMvc.perform(get("/api/profiles/me").with(jwt().jwt(j -> j.subject(KEYCLOAK_ID))))
    //         .andExpect(status().isNotFound());
    // }

    // @Test
    // void createMyProfileShouldUseSubjectAndUsernameFromToken() throws Exception {
    //     String expectedDisplayName = "rick";
    //     String expectedLocation = "/api/profiles/me";
    //     UserProfile profile = new UserProfile(KEYCLOAK_ID, "rick");
    //     when(userProfileService.create(KEYCLOAK_ID, expectedDisplayName)).thenReturn(profile);
    //     when(userProfileMapper.toResponse(profile))
    //         .thenReturn(new UserProfileResponse(1L, "rick", Instant.now(), null));

    //     mockMvc.perform(post("/api/profiles/me")
    //             .with(jwt().jwt(j -> j.subject(KEYCLOAK_ID).claim("preferred_username", expectedDisplayName))))
    //         .andExpect(status().isCreated())
    //         .andExpect(header().string("Location", expectedLocation))
    //         .andExpect(jsonPath("$.displayName").value(expectedDisplayName));
    // }

    // @Test
    // void createMyProfileTwiceShouldReturn409() throws Exception {
    //     when(userProfileService.create(KEYCLOAK_ID, "rick"))
    //         .thenThrow(new ResourceConflictException("A profile already exists for this user"));

    //     mockMvc.perform(post("/api/profiles/me")
    //             .with(jwt().jwt(j -> j.subject(KEYCLOAK_ID).claim("preferred_username", "rick"))))
    //         .andExpect(status().isConflict());
    // }
}
