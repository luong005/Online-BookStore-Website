package com.BookShop_Backend.configurations;

import com.BookShop_Backend.filters.JwtTokenFilter;
import com.BookShop_Backend.models.RoleEntity;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
//@EnableMethodSecurity
@EnableWebSecurity
@EnableWebMvc
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    @Value("${app.cors.allowed-origin-patterns}")
    private String allowedOriginPatterns;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore((Filter) jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.PATCH, "/api/book-*").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/book-*").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/book-*").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/book").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/books/import-errors/*").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/admin/dashboard/revenue-*").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/admin/dashboard/best-selling-books-*").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/admin/dashboard/user").hasRole(RoleEntity.ADMIN)
                        .requestMatchers("/payouts/**").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/cart").hasAnyRole(RoleEntity.USER, RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/cart/item").hasAnyRole(RoleEntity.USER, RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/cart/item-*").hasAnyRole(RoleEntity.USER, RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/cart/items").hasAnyRole(RoleEntity.USER, RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/cart/checkout").hasAnyRole(RoleEntity.USER, RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/cart/orders").hasAnyRole(RoleEntity.USER, RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/cart/order-*").hasAnyRole(RoleEntity.USER, RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/cart/order-*").hasAnyRole(RoleEntity.USER, RoleEntity.ADMIN)
                        .requestMatchers("/api/order/**").hasAnyRole(RoleEntity.USER, RoleEntity.ADMIN)
                        .requestMatchers("/api/profile", "/api/update-password", "/api/update-info").hasAnyRole(RoleEntity.ADMIN, RoleEntity.USER)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/payment/**",
                                "/api/login",
                                "/api/refresh",
                                "/api/logout",
                                "/api/register",
                                "/api/books/**",
                                "/api/book-*")
                        .permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Stream.concat(
                        Stream.of(
                                "http://localhost:3000",
                                "http://127.0.0.1:5500",
                                "http://localhost:5173",
                                "https://online-bookstore-website.pages.dev",
                                "https://*.online-bookstore-website.pages.dev"
                        ),
                        Arrays.stream(allowedOriginPatterns.split(","))
                )
                .map(String::trim)
                .filter(originPattern -> !originPattern.isBlank())
                .distinct()
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
