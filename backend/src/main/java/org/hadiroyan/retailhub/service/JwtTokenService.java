package org.hadiroyan.retailhub.service;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hadiroyan.retailhub.model.User;
import org.jboss.logging.Logger;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JwtTokenService {

    private static final Logger LOG = Logger.getLogger(JwtTokenService.class);

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "smallrye.jwt.new-token.lifespan")
    Long tokenLifespan;

    public String generateToken(User user) {
        return generateToken(user, null, tokenLifespan);
    }

    public String generateToken(User user, long expirationSeconds) {
        return buildToken(user, null, expirationSeconds);
    }

    public String generateToken(User user, Object storeData) {
        return buildToken(user, storeData, tokenLifespan);
    }

    public String generateToken(User user, Object storeData, long expirationSeconds) {
        return buildToken(user, storeData, expirationSeconds);
    }

    private String buildToken(User user, Object storeData, long expirationSeconds) {

        Set<String> roles = extractRoles(user);
        Set<String> privileges = extractPrivileges(user);

        LOG.debugf("Generating token for userId=%s", user.id);

        JwtClaimsBuilder builder = Jwt.issuer(issuer)
                .upn(user.email)
                .subject(user.id.toString())
                .groups(roles)
                .claim("fullName", user.fullName)
                .claim("privileges", privileges)
                .claim("provider", user.provider)
                .claim("emailVerified", user.emailVerified)
                .claim("enabled", user.enabled)
                .expiresIn(Duration.ofSeconds(expirationSeconds));

        if (storeData != null) {
            addStoreClaims(builder, roles, storeData);
        }

        return builder.sign();
    }

    private Set<String> extractRoles(User user) {
        return user.userRoles.stream()
                .map(ur -> ur.role.name)
                .collect(Collectors.toSet());
    }

    private Set<String> extractPrivileges(User user) {
        return user.userRoles.stream()
                .flatMap(ur -> ur.role.privileges.stream())
                .map(p -> p.name)
                .collect(Collectors.toSet());
    }

    private void addStoreClaims(JwtClaimsBuilder builder, Set<String> roles, Object storeData) {
        String claimName = getStoreClaimName(roles);

        if (claimName != null) {
            builder.claim(claimName, storeData);
            LOG.debugf("Added %s claim to token", claimName);
        }
    }

    private String getStoreClaimName(Set<String> roles) {
        if (roles.contains("OWNER")) {
            return "stores"; // Array of stores
        } else if (roles.contains("ADMIN") || roles.contains("MANAGER") || roles.contains("STAFF")) {
            return "assignedStore"; // Single store object
        }
        return null; // No store claim for SUPER_ADMIN and CUSTOMER
    }
}
