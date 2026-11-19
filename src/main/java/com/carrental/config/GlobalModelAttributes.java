package com.carrental.config;

import com.carrental.entity.User;
import com.carrental.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;

    public GlobalModelAttributes(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private Authentication currentAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }

    @ModelAttribute("loggedIn")
    public boolean loggedIn() {
        return currentAuthentication() != null;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        Authentication auth = currentAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @ModelAttribute("displayName")
    public String displayName() {
        Authentication auth = currentAuthentication();
        if (auth == null) {
            return null;
        }
        return userRepository.findByEmail(auth.getName())
                .map(User::getUserName)
                .orElse(auth.getName());
    }
}