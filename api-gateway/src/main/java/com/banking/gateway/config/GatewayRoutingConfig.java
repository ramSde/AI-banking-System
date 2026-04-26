package com.banking.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Routing Configuration
 * 
 * Defines all routes from the API Gateway to downstream microservices.
 * Each route maps a path pattern to a target service URI.
 * 
 * Routing Strategy:
 * - All routes use path-based routing (/api/v1/{service}/**)
 * - Routes are loaded from application.yml via GatewayProperties
 * - Each route strips the /api prefix before forwarding to the service
 * - Circuit breaker and retry filters are applied via default-filters in application.yml
 * 
 * Route Configuration:
 * - Identity & Security services (ports 8081-8084)
 * - User Context services (ports 8085-8086)
 * - Core Banking services (ports 8087-8088)
 * - Safety services (ports 8089-8090)
 * - Communication services (port 8091)
 * - AI Infrastructure services (ports 8092-8093)
 * - AI Intelligence services (ports 8094-8095)
 * - Multimodal Interaction services (ports 8096-8099)
 * - User Experience services (ports 8100-8101)
 * - Financial Intelligence services (ports 8102-8107)
 * - Bank-Grade Systems services (ports 8108-8109)
 * - Additional services (port 8110)
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayRoutingConfig {

    private final GatewayProperties gatewayProperties;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring gateway routes for {} services", gatewayProperties.getRoutes().size());

        RouteLocatorBuilder.Builder routesBuilder = builder.routes();

        gatewayProperties.getRoutes().forEach((serviceName, routeProps) -> {
            String routeId = serviceName + "-route";
            String uri = routeProps.getUri();
            String path = routeProps.getPath();

            log.debug("Registering route: {} -> {} (path: {})", routeId, uri, path);

            routesBuilder.route(routeId, r -> r
                    .path(path)
                    .filters(f -> f
                            .stripPrefix(0)
                            .addRequestHeader("X-Gateway-Service", serviceName)
                            .addRequestHeader("X-Forwarded-Proto", "https")
                    )
                    .uri(uri)
            );
        });

        log.info("Gateway routing configuration completed successfully");
        return routesBuilder.build();
    }
}
