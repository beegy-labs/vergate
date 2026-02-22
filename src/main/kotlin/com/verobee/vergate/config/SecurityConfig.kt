package com.verobee.vergate.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/v1/init/**").permitAll()
                    .requestMatchers("/api/v1/legal/**").permitAll()
                    .requestMatchers("/api/v1/admin/**").permitAll() // TODO: JWT auth
                    .requestMatchers(
                        "/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**",
                        "/actuator/**",
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .build()
}
