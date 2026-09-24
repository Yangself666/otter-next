package com.alibaba.otter.manager.web.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import com.alibaba.otter.manager.biz.user.dal.UserDAO;
import com.alibaba.otter.shared.common.utils.SecurityUtils;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider managerAuthentication(UserDAO users, PasswordEncoder encoder) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) {
                var user = users.findByName(authentication.getName());
                String password = String.valueOf(authentication.getCredentials());
                boolean bcrypt = user != null && user.getPassword() != null && user.getPassword().startsWith("$2");
                boolean matches = user != null && user.getPassword() != null
                    && password.getBytes(StandardCharsets.UTF_8).length <= 72 && (bcrypt ? encoder.matches(password, user.getPassword())
                    : MessageDigest.isEqual(SecurityUtils.getPassword(password).getBytes(StandardCharsets.UTF_8),
                                            user.getPassword().getBytes(StandardCharsets.UTF_8)));
                if (!matches || user.getAuthorizeType() == null || user.getAuthorizeType().isAnonymous()) {
                    throw new BadCredentialsException("用户名或密码错误");
                }
                // 已有账号成功认证后升级密码摘要，后续登录统一使用 BCrypt
                if (!bcrypt) {
                    user.setPassword(encoder.encode(password));
                    users.updateUser(user);
                }
                var result = UsernamePasswordAuthenticationToken.authenticated(user.getName(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getAuthorizeType().name())));
                result.setDetails(Map.of("id", user.getId(), "name", user.getName(),
                                         "role", user.getAuthorizeType().name()));
                return result;
            }

            @Override
            public boolean supports(Class<?> type) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(type);
            }
        };
    }

    @Bean
    public SecurityFilterChain managerSecurity(HttpSecurity http, AuthenticationProvider provider, UserDAO users) throws Exception {
        http.addFilterAfter(new AccountStatusFilter(users),
                org.springframework.security.web.context.SecurityContextHolderFilter.class)
            .authenticationProvider(provider)
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/", "/index.html", "/assets/**", "/favicon.svg", "/api/session", "/api/login",
                                 "/actuator/health", "/error").permitAll()
                .requestMatchers("/api/config/users/**", "/api/settings/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/channels/*/start", "/api/channels/*/stop")
                    .hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/api/**").hasRole("ADMIN")
                .anyRequest().denyAll())
            .formLogin(login -> login.loginProcessingUrl("/api/login")
                .successHandler((request, response, authentication) -> response.setStatus(204))
                .failureHandler((request, response, error) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"用户名或密码错误\"}");
                }))
            .logout(logout -> logout.logoutUrl("/api/logout")
                .logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, error) -> response.sendError(401))
                .accessDeniedHandler((request, response, error) -> response.sendError(403)));
        return http.build();
    }
}
