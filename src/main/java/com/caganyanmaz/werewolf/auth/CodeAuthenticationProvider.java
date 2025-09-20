package com.caganyanmaz.werewolf.auth;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CodeAuthenticationProvider implements AuthenticationProvider {

    @Value("very-secret-code")   // TODO: change this before deployment, 
    private String accessCode;

    @Override
    public Authentication authenticate(Authentication authentication) {
        if (!(authentication instanceof CodeAuthenticationToken token)) return null;

        String submitted = (String) token.getCredentials();
        if (submitted != null && submitted.equals(accessCode)) {
            // Give each login a unique principal
            String username = "player-" + UUID.randomUUID();
            return new CodeAuthenticationToken(
                username,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }
        return null; // fall through to failure
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
