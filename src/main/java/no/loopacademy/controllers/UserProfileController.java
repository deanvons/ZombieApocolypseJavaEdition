package no.loopacademy.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import no.loopacademy.dtos.response.UserProfileResponse;
import no.loopacademy.mappers.UserProfileMapper;
import no.loopacademy.models.userprofile.UserProfile;
import no.loopacademy.services.UserProfileService;

/*
 * Everything here is "/me": the Keycloak id always comes from the verified
 * token (jwt.getSubject()), never from the URL or body. Otherwise anyone could
 * read or create another user's profile by sending their id.
 */
@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserProfileMapper userProfileMapper;

    public UserProfileController(UserProfileService userProfileService, UserProfileMapper userProfileMapper) {
        this.userProfileService = userProfileService;
        this.userProfileMapper = userProfileMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        UserProfile profile = userProfileService.findByKeycloakId(jwt.getSubject());
        return ResponseEntity.ok(userProfileMapper.toResponse(profile));
    }

    // No request body: the display name is the Keycloak username from the token
    @PostMapping("/me")
    public ResponseEntity<UserProfileResponse> createMyProfile(@AuthenticationPrincipal Jwt jwt) {
        String displayName = jwt.getClaimAsString("preferred_username");
        if (displayName == null || displayName.isBlank()) {
            displayName = jwt.getSubject();
        }
        UserProfile profile = userProfileService.create(jwt.getSubject(), displayName);
        return ResponseEntity
                .created(URI.create("/api/profiles/me"))
                .body(userProfileMapper.toResponse(profile));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileResponse>> getAllSurvivors(@AuthenticationPrincipal Jwt jwt) {
        List<UserProfile> userProfiles = userProfileService.findAll();
        return ResponseEntity.ok(userProfileMapper.toResponses(userProfiles));

    }

}
