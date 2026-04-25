package com.banking.rag.filter;

import com.banking.rag.util.JwtValidator;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;
    private final Tracer tracer;

    public JwtAuthenticationFilter(JwtValidator jwtValidator, Tracer tracer) {
        this.jwtValidator = jwtValidator;
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String traceId = getTraceId();
        
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No JWT token found in request headers, traceId: {}", traceId);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);

            if (jwtValidator.validateToken(token) && !jwtValidator.isTokenExpired(token)) {
                String username = jwtValidator.extractUsername(token);
                List<String> roles = jwtValidator.extractRoles(token);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("JWT authentication successful for user: {}, traceId: {}", username, traceId);
            } else {
                log.warn("Invalid or expired JWT token, traceId: {}", traceId);
            }
        } catch (Exception e) {
            log.error("JWT authentication failed, traceId: {}, error: {}", traceId, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "no-trace-id";
    }
}
