package com.marutixchange.user_service.security;

import com.marutixchange.user_service.repository.UserRepository;
import com.marutixchange.user_service.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // ✅ CRITICAL FIX: Skip auth endpoints
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);

            if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByEmail(email)
                        .orElse(null);

                if (user != null && jwtUtil.validateToken(token)) {

                    var authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                    );

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user.getEmail(),
                                    null,
                                    authorities
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);
                }
            }

        } catch (Exception ex) {
            System.out.println("JWT Error: " + ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}