package com.mygordienko.brennon.core.auth.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

import com.mygordienko.brennon.core.auth.adapter.jdbc.JdbcBrennonUserDetailsStorage;
import com.mygordienko.brennon.core.auth.adapter.web.HxRefreshHeaderAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class CoreSecurityConfiguration {

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        var htmxEntryPoint = new HxRefreshHeaderAuthenticationEntryPoint();
        var htmxRequestMatcher = new RequestHeaderRequestMatcher("HX-Request");

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/css/**", "/dist/**", "/js/**", "/images/**", "/svg/**", "/favicon.ico", "/actuator/health/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                    // NOTE: custom entry point for HTMX requests
                    .defaultAuthenticationEntryPointFor(htmxEntryPoint, htmxRequestMatcher)
                );

        return http.build();
    }

    /**
     * BrennonUserPort + UserDetailsService
     */
    @Bean
    public JdbcBrennonUserDetailsStorage userDetailsService(NamedParameterJdbcTemplate jdbcTemplate) {
        return new JdbcBrennonUserDetailsStorage(jdbcTemplate);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}