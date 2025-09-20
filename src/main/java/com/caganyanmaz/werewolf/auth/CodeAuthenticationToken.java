package com.caganyanmaz.werewolf.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

public class CodeAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;  // before auth: null; after auth: unique username
    private final String code;

    // unauthenticated
    public CodeAuthenticationToken(String code) {
        super(null);
        this.principal = null;
        this.code = code;
        setAuthenticated(false);
    }

    // authenticated
    public CodeAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.code = null;
        setAuthenticated(true);
    }

    @Override public Object getCredentials() { return code; }
    @Override public Object getPrincipal() { return principal; }
}
