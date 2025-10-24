package com.example.bankapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

   @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .authorizeHttpRequests()
            .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll() // accès public à la page d'accueil et ressources statiques
            .requestMatchers("/create").hasRole("ADMIN") // seul admin peut créer
            .requestMatchers("/transfer").hasRole("USER") // seul user peut transférer
            .requestMatchers("/clients", "/solde", "/comptes").hasAnyRole("USER", "ADMIN") // accessible aux deux
            .anyRequest().authenticated()
        .and()
        .httpBasic();
    return http.build();
}


    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username("user")
            .password("{noop}password")
            .roles("USER")
            .build();

        UserDetails admin = User.builder()
            .username("admin")
            .password("{noop}password")
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(user, admin);
    }
}