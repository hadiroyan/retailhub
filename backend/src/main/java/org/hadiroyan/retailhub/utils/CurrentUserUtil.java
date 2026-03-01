package org.hadiroyan.retailhub.utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hadiroyan.retailhub.exception.UnauthorizedException;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CurrentUserUtil {

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;

    // =========================================================================
    // Identity
    // =========================================================================

    /**
     * Returns the current user's UUID from JWT sub claim.
     */
    public UUID getUserId() {
        if (identity.isAnonymous()) {
            throw new UnauthorizedException("User not authenticated");
        }
        try {
            return UUID.fromString(identity.getPrincipal().getName());
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid user ID format in token");
        }
    }

    /**
     * Returns the current user's UUID wrapped in Optional.
     * Returns Optional.empty() if anonymous or sub claim is invalid.
     * Use this for @PermitAll endpoints where anonymous is a valid caller.
     */
    public Optional<UUID> getUserIdOptional() {
        if (identity.isAnonymous()) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(identity.getPrincipal().getName()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the current user's email from JWT upn claim.
     */
    public String getEmail() {
        if (identity.isAnonymous()) {
            throw new UnauthorizedException("User not authenticated");
        }
        String email = jwt.getClaim("upn");
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Email not found in token");
        }
        return email;
    }

    /**
     * Returns all roles of the current user from JWT groups claim.
     * Returns empty Set for anonymous users.
     */
    public Set<String> getRoles() {
        if (identity.isAnonymous()) {
            return Set.of();
        }
        Set<String> roles = identity.getRoles();
        if (roles == null || roles.isEmpty()) {
            throw new UnauthorizedException("No roles found in token");
        }
        return roles;
    }

    public boolean isAnonymous() {
        return identity.isAnonymous();
    }

    public boolean hasRole(String role) {
        return identity.hasRole(role);
    }

    // =========================================================================
    // Store claims — for READ operations only (JWT as cache)
    // =========================================================================

    /**
     * For OWNER: returns list of store IDs from "stores" JWT claim.
     *
     * JWT structure: "stores": [ {id, name, slug, status}, ... ]
     */
    public List<UUID> getOwnerStoreIds() {
        List<Map<String, Object>> stores = jwt.getClaim("stores");

        if (stores == null || stores.isEmpty()) {
            return List.of();
        }

        return stores.stream()
                .map(store -> UUID.fromString((String) store.get("id")))
                .toList();
    }

    /**
     * For OWNER: check if a specific storeId exists in JWT "stores" claim.
     * Fast ownership check for read ops — no DB needed.
     */
    public boolean ownsStore(UUID storeId) {
        return getOwnerStoreIds().stream().anyMatch(id -> id.equals(storeId));
    }

    /**
     * For ADMIN/MANAGER/STAFF: returns assigned store ID from "assignedStore" JWT
     * claim.
     *
     * JWT structure: "assignedStore": {id, name, slug, status}
     */
    public Optional<UUID> getAssignedStoreId() {
        Map<String, Object> assignedStore = jwt.getClaim("assignedStore");

        if (assignedStore == null || !assignedStore.containsKey("id")) {
            return Optional.empty();
        }

        return Optional.of(UUID.fromString((String) assignedStore.get("id")));
    }

    /**
     * For ADMIN/MANAGER/STAFF: check if user is assigned to a specific store.
     * Fast check for read ops — no DB needed.
     */
    public boolean isAssignedToStore(UUID storeId) {
        return getAssignedStoreId()
                .map(id -> id.equals(storeId))
                .orElse(false);
    }

    /**
     * General helper — returns true if current user has access to storeId.
     * Covers: SUPER_ADMIN (all stores), OWNER (own stores), ADMIN/MANAGER/STAFF
     * (assigned store).
     *
     * Use for READ operations only — write ops must validate against DB.
     */
    public boolean hasStoreAccess(UUID storeId) {
        if (hasRole("SUPER_ADMIN"))
            return true;
        if (hasRole("OWNER"))
            return ownsStore(storeId);
        return isAssignedToStore(storeId); // ADMIN, MANAGER, STAFF
    }
}