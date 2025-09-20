package com.caganyanmaz.werewolf;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.caganyanmaz.werewolf.auth.CodeAuthenticationFilter;
import com.caganyanmaz.werewolf.auth.CodeAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public ProviderManager authenticationManager(CodeAuthenticationProvider provider) {
        return new ProviderManager(List.of(provider));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ProviderManager authManager) throws Exception {
        var matcher = new AntPathRequestMatcher("/enter", "POST");

        // Our code filter handles POST /enter; GET /enter is the login page
        var codeFilter = new CodeAuthenticationFilter(authManager, matcher);

        var sessionRepo = new HttpSessionSecurityContextRepository();

        http
            .securityContext(sc -> sc
                    .securityContextRepository(sessionRepo)
                    .requireExplicitSave(false) // <- let Spring save automatically
            )
            .sessionManagement(sm -> sm.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(req -> req
                    .requestMatchers("/enter", "/error", "/css/**", "/js/**").permitAll()
                    .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/enter")))
            // We are not using Spring's formLogin; our custom filter logs users in.
            .addFilterBefore(codeFilter, UsernamePasswordAuthenticationFilter.class)
            .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/enter").permitAll())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/enter")) // optional: keep CSRF; if using Thymeleaf form,
                                                                    // keep it enabled
        ;

        return http.build();
    }
}
