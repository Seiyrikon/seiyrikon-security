package com.seiyrikon.seiyrikon_security.util;

import com.seiyrikon.seiyrikon_security.exception.auth.SeiyrikonSecurityAuthenticationException;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class SeiyrikonSecurityPrincipal implements UserDetails {

    private Object userId;
    private List<GrantedAuthority> authorities;

    // ← static helper
    public static SeiyrikonSecurityPrincipal getCurrent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw SeiyrikonSecurityAuthenticationException.authenticatedUserNotFound();
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof SeiyrikonSecurityPrincipal)) {
            throw SeiyrikonSecurityAuthenticationException.invalidPrincipalType();
        }

        return (SeiyrikonSecurityPrincipal) principal;
    }

    // ← convenience methods
    public static Object getCurrentUserId() {
        return getCurrent().getUserId();
    }

    public static List<String> getCurrentRoles() {
        return getCurrent().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public static boolean currentHasRole(String role) {
        return getCurrent().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() { return String.valueOf(userId); }

    @Override
    public String getPassword() { return null; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
