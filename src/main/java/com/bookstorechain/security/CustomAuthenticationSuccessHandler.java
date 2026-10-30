package com.bookstorechain.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean isStaffOrAdmin = authorities.stream().anyMatch(role ->
                role.getAuthority().equals("ROLE_ADMIN")
                        || role.getAuthority().equals("ROLE_MANAGER")
                        || role.getAuthority().equals("ROLE_STAFF"));

        boolean isShipper = authorities.stream().anyMatch(role -> role.getAuthority().equals("ROLE_SHIPPER"));

        if (isShipper) {
            response.sendRedirect("/shipper/orders");
        } else if (isStaffOrAdmin) {
            response.sendRedirect("/admin/dashboard");
        } else {
            response.sendRedirect("/");
        }
    }
}