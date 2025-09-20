package com.caganyanmaz.werewolf.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CodeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public CodeAuthenticationFilter(AuthenticationManager authenticationManager, RequestMatcher matcher) {
        super(matcher);
        setAuthenticationManager(authenticationManager);
        // Where to go on success:
        setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler("/"));
    }

    @Override
    public org.springframework.security.core.Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response) {

        String code = request.getParameter("code");
        return this.getAuthenticationManager().authenticate(new CodeAuthenticationToken(code));
    }
}
