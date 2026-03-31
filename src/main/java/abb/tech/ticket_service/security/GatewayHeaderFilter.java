package abb.tech.ticket_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.Arrays;
import java.util.List;

@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader("X-USER-ID");
        String username = request.getHeader("X-USER-NAME");
        String authorities = request.getHeader("X-USER-AUTHORITIES");

        if (userId != null && username != null && authorities != null) {
            List<SimpleGrantedAuthority> authList = Arrays.stream(authorities.split(",")).map(SimpleGrantedAuthority::new).toList();
            UsernamePasswordAuthenticationToken passwordAuthentication = new UsernamePasswordAuthenticationToken(userId, username, authList);
            SecurityContextHolder.getContext().setAuthentication(passwordAuthentication);

        }
        filterChain.doFilter(request,response);
    }
}
