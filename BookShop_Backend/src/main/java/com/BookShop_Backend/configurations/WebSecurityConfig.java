package com.BookShop_Backend.configurations;

import com.BookShop_Backend.filters.JwtTokenFilter;
import com.BookShop_Backend.models.RoleEntity;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
//@EnableMethodSecurity
@EnableWebSecurity
@EnableWebMvc
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))// Phải disable CSRF để test POST từ Postman dễ hơn
                .addFilterBefore((Filter) jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.PATCH,"/api/book-*").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.DELETE,"/api/book-*").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.POST,"/api/book-*").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.POST,"/api/book").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET,"/api/admin/dashboard/revenue-*").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET,"/api/admin/dashboard/best-selling-books-*").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET,"/api/admin/dashboard/user").hasRole(RoleEntity.ADMIN)
                        .requestMatchers("/payouts/**").hasRole(RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET,"/api/cart").hasAnyRole(RoleEntity.USER,RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.PUT,"/api/cart/item").hasAnyRole(RoleEntity.USER,RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.DELETE,"/api/cart/item-*").hasAnyRole(RoleEntity.USER,RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.POST,"/api/cart/items").hasAnyRole(RoleEntity.USER,RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET,"/api/cart/checkout").hasAnyRole(RoleEntity.USER,RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET,"/api/cart/orders").hasAnyRole(RoleEntity.USER,RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.GET,"/api/cart/order-*").hasAnyRole(RoleEntity.USER,RoleEntity.ADMIN)
                        .requestMatchers(HttpMethod.DELETE,"/api/cart/order-*").hasAnyRole(RoleEntity.USER,RoleEntity.ADMIN)
                        .requestMatchers("/api/order/**").hasAnyRole(RoleEntity.USER,RoleEntity.ADMIN)
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

//    @Bean
//    public CorsFilter corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // Domain của frontend gọi đến
//        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:5500"));
//
//        // Các HTTP method được phép
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//
//        // Các header được phép gửi lên (Quan trọng: phải có Authorization nếu dùng JWT)
//        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
//
//        // Header được phép trả về cho trình duyệt đọc (ví dụ nếu bạn trả JWT qua header)
//        configuration.setExposedHeaders(List.of("Authorization"));
//
//        configuration.setAllowCredentials(true); // Cho phép đính kèm cookie hoặc thông tin xác thực
//
//        // Áp dụng cấu hình này cho tất cả các endpoint (/**)
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return new CorsFilter(source);
//    }
}
