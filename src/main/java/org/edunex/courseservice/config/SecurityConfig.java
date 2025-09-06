package org.edunex.courseservice.config;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;

import java.text.ParseException;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF as this is a stateless API
                .csrf(csrf -> csrf.disable())
                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // All requests must be authenticated (i.e., have a valid-looking JWT)
                        .anyRequest().authenticated()
                )
                // Configure the OAuth2 resource server to use JWTs
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                // Tell Spring to use our custom decoder bean
                                .decoder(jwtDecoder())
                        )
                );
        return http.build();
    }

    /**
     * Creates a custom JwtDecoder bean that decodes the token without verifying the signature.
     * This is suitable when a trusted party (like an API gateway) has already performed verification.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                try {
                    // Use the Nimbus library to parse the JWT string
                    JWT jwt = JWTParser.parse(token);

                    // Get claims and headers
                    Map<String, Object> claims = jwt.getJWTClaimsSet().getClaims();
                    Map<String, Object> headers = jwt.getHeader().toJSONObject();

                    // Create a Spring Security Jwt object
                    return new Jwt(
                            token,
                            jwt.getJWTClaimsSet().getIssueTime().toInstant(),
                            jwt.getJWTClaimsSet().getExpirationTime().toInstant(),
                            headers,
                            claims
                    );
                } catch (ParseException e) {
                    throw new JwtException("Failed to parse JWT", e);
                }
            }
        };
    }
}
