package org.hadiroyan.retailhub.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "privileges")
public class Privilege extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(length = 100, nullable = false, unique = true)
    public String name;

    @Column(length = 50, nullable = false)
    public String resource;

    @Column(columnDefinition = "TEXT")
    public String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @ManyToMany(mappedBy = "privileges", fetch = FetchType.LAZY)
    public Set<Role> roles = new HashSet<>();

    public Privilege() {
    }

    public Privilege(String name, String resource, String description) {
        this.name = name;
        this.resource = resource;
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Privilege))
            return false;
        Privilege privilege = (Privilege) obj;
        return name != null && name.equals(privilege.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Privilege{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", resource='" + resource + '\'' +
                '}';
    }
}
