// // package no.loopacademy.controllers;

// // import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// // import org.springframework.context.annotation.Import;

// // import no.loopacademy.config.authConfig;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.context.annotation.Import;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.test.web.servlet.MockMvc;

// //     // private static final String KEYCLOAK_ID = "3f2a9c1e-0000-4000-8000-000000000001";

// // Security stays ON here (unlike SurvivorControllerTest), so these tests also prove

// //     // @MockitoBean
// //     // private UserProfileMapper userProfileMapper;

// //     // @Test
// //     // void getMyProfileWithoutTokenShouldReturn401() throws Exception {
// //     //     mockMvc.perform(get("/api/profiles/me"))
// //     // }

// //     // @Test
// //     // void getMyProfileShouldLookUpByTokenSubject() throws Exception {
// //     //     String expectedDisplayName = "rick";
// //     //     UserProfile profile = new UserProfile(KEYCLOAK_ID, "rick");
// //     //     when(userProfileService.findByKeycloakId(KEYCLOAK_ID)).thenReturn(profile);
// //     //     when(userProfileMapper.toResponse(profile))
// //     //         .thenReturn(new UserProfileResponse(1L, expectedDisplayName, Instant.now(), null));

// <<<<<<< HEAD
// //     //     mockMvc.perform(get("/api/profiles/me").with(jwt().jwt(j -> j.subject(KEYCLOAK_ID))))
// //     //         .andExpect(status().isOk())
// //     //         .andExpect(jsonPath("$.displayName").value(expectedDisplayName))
// //     //         .andExpect(jsonPath("$.survivorId").isEmpty());
// //     // }

// //     // @Test
//         mockMvc.perform(get("/api/profiles/me"))
//             .andExpect(status().isUnauthorized());
//     }

//     @Test
//     void getMyProfileShouldLookUpByTokenSubject() throws Exception {

//         UserProfile profile = new UserProfile(KEYCLOAK_ID, expectedDisplayName);
//         when(userProfileService.findByKeycloakId(KEYCLOAK_ID)).thenReturn(profile);
//         when(userProfileMapper.toResponse(profile))
//             .thenReturn(new UserProfileResponse(userProfileId, expectedDisplayName, Instant.now(), null));
// >>>>>>> 3fdf3edd450223c82becb826e594d7295130c7dd

// //     //     mockMvc.perform(get("/api/profiles/me").with(jwt().jwt(j -> j.subject(KEYCLOAK_ID))))
// //     //         .andExpect(status().isNotFound());
// //     // }

// //     // @Test
// //     // void createMyProfileShouldUseSubjectAndUsernameFromToken() throws Exception {
// //     //     String expectedLocation = "/api/profiles/me";
// //     //     UserProfile profile = new UserProfile(KEYCLOAK_ID, "rick");
// //     //     when(userProfileService.create(KEYCLOAK_ID, expectedDisplayName)).thenReturn(profile);
// //     //     when(userProfileMapper.toResponse(profile))
// //     //         .thenReturn(new UserProfileResponse(1L, "rick", Instant.now(), null));

// //     //     mockMvc.perform(post("/api/profiles/me")
// //     //             .with(jwt().jwt(j -> j.subject(KEYCLOAK_ID).claim("preferred_username", expectedDisplayName))))
// //     //         .andExpect(status().isCreated())
// //     //         .andExpect(header().string("Location", expectedLocation))
// //     //         .andExpect(jsonPath("$.displayName").value(expectedDisplayName));
// //     // }

//     @Test
//     void createMyProfileShouldUseSubjectAndUsernameFromToken() throws Exception {

//         String expectedLocation = "/api/profiles/me";
//         UserProfile profile = new UserProfile(KEYCLOAK_ID, expectedDisplayName);
//         when(userProfileService.create(KEYCLOAK_ID, expectedDisplayName)).thenReturn(profile);
//         when(userProfileMapper.toResponse(profile))
//             .thenReturn(new UserProfileResponse(userProfileId, expectedDisplayName, Instant.now(), null));

//         mockMvc.perform(post("/api/profiles/me")
//                 .with(jwt().jwt(j -> j.subject(KEYCLOAK_ID).claim("preferred_username", expectedDisplayName))))
//             .andExpect(status().isCreated())
//             .andExpect(header().string("Location", expectedLocation))
//             .andExpect(jsonPath("$.displayName").value(expectedDisplayName));
//     }

//     @Test
//     void createMyProfileTwiceShouldReturn409() throws Exception {
//         when(userProfileService.create(KEYCLOAK_ID, expectedDisplayName))
//             .thenThrow(new ResourceConflictException("A profile already exists for this user"));

//         mockMvc.perform(post("/api/profiles/me")
//                 .with(jwt().jwt(j -> j.subject(KEYCLOAK_ID).claim("preferred_username", expectedDisplayName))))
//             .andExpect(status().isConflict());
//     }
// }
