package com.example.demo.config;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// If already authenticated, skip
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				String adminUsername = (String) session.getAttribute("admin_session");
				String adminRole = (String) session.getAttribute("admin_role");

				if (adminUsername != null && adminRole != null) {
					Collection<? extends GrantedAuthority> authorities = switch (adminRole) {
						case "SUPER_ADMIN" -> List.of(new SimpleGrantedAuthority("SUPER_ADMIN"),
								new SimpleGrantedAuthority("ADMIN"));
						case "ADMIN" -> List.of(new SimpleGrantedAuthority("ADMIN"));
						default -> List.of();
					};

					if (!authorities.isEmpty()) {
						AbstractAuthenticationToken auth = new AbstractAuthenticationToken(authorities) {
							private static final long serialVersionUID = 1L;
							@Override public Object getCredentials() { return ""; }
							@Override public Object getPrincipal() { return adminUsername; }
						};
						auth.setAuthenticated(true);
						SecurityContextHolder.getContext().setAuthentication(auth);
					}
				}
			}
		}

		filterChain.doFilter(request, response);
	}
}

