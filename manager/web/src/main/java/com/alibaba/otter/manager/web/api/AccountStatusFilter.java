package com.alibaba.otter.manager.web.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.alibaba.otter.manager.biz.user.dal.UserDAO;

public class AccountStatusFilter extends OncePerRequestFilter {

    private final UserDAO users;

    public AccountStatusFilter(UserDAO users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (request.getRequestURI().startsWith("/api/")
            && authentication instanceof UsernamePasswordAuthenticationToken && authentication.isAuthenticated()) {
            var user = users.findByName(authentication.getName());
            // 每次请求读取当前角色，使账号删除和权限调整立即作用于已登录会话
            if (user == null || user.getAuthorizeType() == null || user.getAuthorizeType().isAnonymous()) {
                context.setAuthentication(null);
                if (request.getSession(false) != null) request.getSession(false).invalidate();
            } else {
                var current = UsernamePasswordAuthenticationToken.authenticated(user.getName(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getAuthorizeType().name())));
                current.setDetails(Map.of("id", user.getId(), "name", user.getName(), "role", user.getAuthorizeType().name()));
                context.setAuthentication(current);
            }
        }
        chain.doFilter(request, response);
    }
}
