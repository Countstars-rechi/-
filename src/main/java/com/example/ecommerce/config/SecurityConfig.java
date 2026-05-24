package com.example.ecommerce.config;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserService userService) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .antMatchers("/login", "/register", "/products", "/product/detail",
                                     "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .antMatchers("/sales/**").hasAnyRole("SALES", "ADMIN")
                        .antMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(roleBasedSuccessHandler(userService))
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    private AuthenticationSuccessHandler roleBasedSuccessHandler(UserService userService) {
        return (HttpServletRequest request, HttpServletResponse response,
                Authentication authentication) -> {
            String username = authentication.getName();
            User user = userService.getCurrentUser(username);
            String redirectUrl = "/products";
            if (user != null) {
                switch (user.getRole()) {
                    case "ADMIN":
                        redirectUrl = "/admin/dashboard";
                        break;
                    case "SALES":
                        redirectUrl = "/sales/dashboard";
                        break;
                }
            }
            response.sendRedirect(redirectUrl);
        };
    }
}
