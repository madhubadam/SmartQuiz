package com.smartassess.config;

import com.smartassess.security.CustomUserDetailsService;
import com.smartassess.security.JwtAuthenticationFilter;
import com.smartassess.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider tokenProvider;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtTokenProvider tokenProvider) {
        this.userDetailsService = userDetailsService;
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors().and().csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login.html",
                    "/register.html",
                    "/faculty-dashboard.html",
                    "/student-dashboard.html",
                    "/question-generator.html",
                    "/question-bank.html",
                    "/create-assessment.html",
                    "/test.html",
                    "/result.html",
                    "/analytics.html",
                    "/css/**",
                    "/js/**",
                    "/favicon.ico",
                    "/h2-console/**",
                    "/api/auth/**",
                    "/api/test/**"
                ).permitAll()
                .requestMatchers("/api/ai/**", "/api/assessments/**", "/api/analytics/**").hasRole("FACULTY")
                .anyRequest().authenticated()
            );

        http.headers().frameOptions().disable(); // for H2 console if used
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
