package org.hadiroyan.retailhub.service;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hadiroyan.retailhub.model.User;
import org.jboss.logging.Logger;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JwtTokenService {

    private static final Logger LOG = Logger.getLogger(JwtTokenService.class);

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "smallrye.jwt.new-token.lifespan")
    Long tokenLifespan;

    public String generateToken(User user) {

        Set<String> roles = user.userRoles.stream()
                .map(ur -> ur.role.name)
                .collect(Collectors.toSet());

        Set<String> privileges = user.userRoles.stream()
                .flatMap(ur -> ur.role.privileges.stream())
                .map(privilege -> privilege.name)
                .collect(Collectors.toSet());

        LOG.infof("User %s with role %s has %d privileges", user.fullName, roles.stream().findFirst().get(),
                privileges.size());
        return Jwt.issuer(issuer)
                .upn(user.email)
                .subject(user.id.toString())
                .groups(roles)
                .claim("email", user.email)
                .claim("name", user.fullName)
                .claim("privileges", privileges)
                .claim("enabled", user.enabled)
                .expiresIn(Duration.ofSeconds(tokenLifespan))
                .sign();
    }

    public String generateToken(User user, long expirationSeconds) {
        Set<String> roles = user.userRoles.stream()
                .map(ur -> ur.role.name)
                .collect(Collectors.toSet());

        Set<String> privileges = user.userRoles.stream()
                .flatMap(ur -> ur.role.privileges.stream())
                .map(p -> p.name)
                .collect(Collectors.toSet());

        return Jwt.issuer(issuer)
                .upn(user.email)
                .subject(user.id.toString())
                .groups(roles)
                .claim("email", user.email)
                .claim("name", user.fullName)
                .claim("privileges", privileges)
                .claim("enabled", user.enabled)
                .expiresIn(Duration.ofSeconds(expirationSeconds))
                .sign();
    }
}
