package com.alibaba.otter.manager.web.api;

import java.util.Map;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {

    @GetMapping("/api/session")
    public Map<String, Object> session(Authentication authentication, CsrfToken csrf) {
        boolean authenticated = authentication != null && authentication.isAuthenticated()
                                && !(authentication instanceof AnonymousAuthenticationToken);
        return Map.of("authenticated", authenticated, "csrfToken", csrf.getToken(),
                      "user", authenticated ? authentication.getDetails() : Map.of());
    }
}
