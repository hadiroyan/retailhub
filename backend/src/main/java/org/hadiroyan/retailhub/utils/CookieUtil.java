package org.hadiroyan.retailhub.utils;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.NewCookie;

@ApplicationScoped
public class CookieUtil {

    @ConfigProperty(name = "cookie.name")
    String cookieName;

    @ConfigProperty(name = "cookie.path")
    String cookiePath;

    @ConfigProperty(name = "cookie.domain")
    String cookieDomain;

    @ConfigProperty(name = "cookie.http-only")
    boolean httpOnly;

    @ConfigProperty(name = "cookie.secure")
    boolean secure;

    @ConfigProperty(name = "cookie.same-site")
    String sameSite;

    @ConfigProperty(name = "cookie.max-age")
    int maxAge;

    public NewCookie createJwtCookie(String token) {
        return new NewCookie.Builder(cookieName)
                .value(token)
                .path(cookiePath)
                .domain(cookieDomain)
                .httpOnly(httpOnly)
                .secure(secure)
                .sameSite(parseSameSite(sameSite))
                .maxAge(maxAge)
                .build();
    }

    public NewCookie createLogoutCookie() {
        return new NewCookie.Builder(cookieName)
                .value("")
                .path(cookiePath)
                .domain(cookieDomain)
                .httpOnly(httpOnly)
                .secure(secure)
                .sameSite(parseSameSite(sameSite))
                .maxAge(0)
                .build();
    }

    public String getCookieName() {
        return cookieName;
    }

    private NewCookie.SameSite parseSameSite(String sameSite) {
        return switch (sameSite.toLowerCase()) {
            case "strict" -> NewCookie.SameSite.STRICT;
            case "lax" -> NewCookie.SameSite.LAX;
            case "none" -> NewCookie.SameSite.NONE;
            default -> NewCookie.SameSite.STRICT; // Default to most secure
        };
    }
}
