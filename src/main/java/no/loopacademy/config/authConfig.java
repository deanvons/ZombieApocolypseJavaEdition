package no.loopacademy.config;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class authConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /*
             * STATELESS disables the server-side session entirely. No HttpSession
             * is created and no session cookie is issued. Every request must carry
             * its own credentials — the JWT. This is the correct policy for a REST
             * API and the reason JWT validation is stateless.
                 */
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth
                        -> auth
                        /*
                 * Swagger UI and the OpenAPI spec are public so the API can be
                 * explored without a token. In production this would be restricted
                 * or removed — here it is kept open to support learning.
                         */
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        /*
                 * Health checks stay public: Render calls /api/health/database
                 * without a token, and a 401 there would fail every deploy.
                         */
                        .requestMatchers("/api/health/**").permitAll()
                        // Every other endpoint requires a valid JWT.
                        .anyRequest().authenticated()
                )
                /*
             * oauth2ResourceServer configures Spring Security to validate incoming
             * JWTs. It fetches the public keys from the JWKS endpoint at startup
             * (derived from the issuer-uri in the active profile) and uses them
             * to verify the signature on every request. No key management here —
             * Keycloak rotates keys; Spring fetches them automatically.
                 */
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    /*
     * Spring only reads the "scope" claim by default (giving SCOPE_profile etc.).
     * Keycloak puts realm roles in a different claim:
     *   "realm_access": { "roles": ["admin", "user", ...] }
     * This maps each one to a Spring role: "admin" -> ROLE_ADMIN, which is what
     * @PreAuthorize("hasRole('ADMIN')") checks for. Uppercased so the Keycloak
     * role can be called "admin" or "ADMIN".
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
                return List.of();
            }
            return roles.stream()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(
                            "ROLE_" + role.toString().toUpperCase(Locale.ROOT)))
                    .toList();
        });
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",                             // local dev (Vite)
                "https://zombieapocolypsejavaeditionfe.onrender.com" // production frontend
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
