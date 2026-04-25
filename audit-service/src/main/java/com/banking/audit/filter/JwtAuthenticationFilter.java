package com.banking.audit.filter;

import com.banking.audit.util.JwtValidator;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtValidator jwtValidator;

    public JwtAuthenticationFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            logger.debug("No JWT token found in request headers");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(BEARER_PREFIX.length());
            Claims claims = jwtValidator.validateToken(token);

            if (jwtValidator.isTokenExpired(claims)) {
                logger.warn("JWT token is expired");
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtValidator.extractUsername(claims);
            List<String> roles = jwtValidator.extractRoles(claims);

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.debug("Successfully authenticated user: {}, roles: {}", username, roles);

        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator/") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui");
    }
}
