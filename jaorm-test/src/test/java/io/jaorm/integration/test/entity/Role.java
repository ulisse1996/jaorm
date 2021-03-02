package io.jaorm.integration.test.entity;

import io.jaorm.annotation.Cacheable;
import io.jaorm.annotation.Column;
import io.jaorm.annotation.Id;
import io.jaorm.annotation.Table;

import java.util.Objects;

@Table(name = "ROLE")
@Cacheable
public class Role {

    @Id
    @Column(name = "ROLE_ID")
    private int roleId;

    @Column(name = "ROLE_NAME")
    private String roleName;

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return roleId == role.roleId && Objects.equals(roleName, role.roleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, roleName);
    }
}
