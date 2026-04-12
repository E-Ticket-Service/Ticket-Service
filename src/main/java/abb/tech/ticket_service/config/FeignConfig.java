package abb.tech.ticket_service.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

import java.util.stream.Collectors;

@Configuration
@Slf4j
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String userId = authentication.getName();
                Object credentials = authentication.getCredentials();
                String username = (credentials instanceof String) ? (String) credentials : null;
                String authorities = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));

                log.debug("Adding security headers to Feign request for user: {}", userId);
                requestTemplate.header("X-USER-ID", userId);
                if (username != null) {
                    requestTemplate.header("X-USER-NAME", username);
                }
                requestTemplate.header("X-USER-AUTHORITIES", authorities);
            } else {
                log.warn("SecurityContext is empty. Feign request sent without user headers.");
                // Burada opsional olaraq sistem-daxili (məsələn, "SYSTEM") header-lər əlavə edilə bilər
                // requestTemplate.header("X-USER-ID", "SYSTEM");
            }
        };
    }
}
