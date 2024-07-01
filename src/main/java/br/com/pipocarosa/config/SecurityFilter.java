package br.com.pipocarosa.config;

import br.com.pipocarosa.models.enums.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityFilter {

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrfConfig -> csrfConfig.disable())
                .sessionManagement(sessionMangConfig ->
                        sessionMangConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(
                        authConfig -> {
                            authConfig.requestMatchers(HttpMethod.POST, "/register").permitAll();
                            authConfig.requestMatchers(HttpMethod.POST, "/login").permitAll();
                            authConfig.requestMatchers("/error").permitAll();
                            authConfig.requestMatchers(HttpMethod.GET, "/users")
                                    .hasAuthority(Permission.READ_ALL_DATA.name());
                            authConfig.requestMatchers(HttpMethod.GET, "/user")
                                    .hasAuthority(Permission.READ_PERSONAL_DATA.name());
                            authConfig.anyRequest().denyAll();
                        }
                );
        return http.build();
    }




















}
