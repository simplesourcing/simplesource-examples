package io.simplesource.example.demo.web;


import io.simplesource.example.demo.HealthcheckService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(1)
public class HealthcheckFilter implements Filter {
    private final HealthcheckService healthcheckService;

    public HealthcheckFilter(HealthcheckService healthcheckService) {
        this.healthcheckService = healthcheckService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!healthcheckService.isHealthy()) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Healthcheck error");
        } else {
            chain.doFilter(request, response);
        }
    }
}
