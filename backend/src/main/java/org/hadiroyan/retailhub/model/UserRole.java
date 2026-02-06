package org.hadiroyan.retailhub.model;

import java.util.Objects;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_roles")
public class UserRole extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    public Role role;

    @Column(name = "store_id")
    public UUID storeId;

    public UserRole() {
    }

    public UserRole(User user, Role role, UUID storeId) {
        this.user = user;
        this.role = role;
        this.storeId = storeId;
    }

    public UserRole(User user, Role role) {
        this(user, role, null);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof UserRole))
            return false;
        UserRole userRole = (UserRole) obj;
        return Objects.equals(user, userRole.user) &&
                Objects.equals(role, userRole.role) &&
                Objects.equals(storeId, userRole.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, role, storeId);
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "id=" + id +
                ", user=" + (user != null ? user.email : null) +
                ", role=" + (role != null ? role.name : null) +
                ", storeId=" + storeId +
                '}';
    }
}
