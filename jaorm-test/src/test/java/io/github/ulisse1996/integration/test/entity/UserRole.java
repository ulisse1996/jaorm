package io.github.ulisse1996.integration.test.entity;

import io.github.ulisse1996.annotation.Column;
import io.github.ulisse1996.annotation.Id;
import io.github.ulisse1996.annotation.Relationship;
import io.github.ulisse1996.annotation.Table;

@Table(name = "USER_ROLE")
public class UserRole {

    @Id
    @Column(name = "USER_ID")
    private int userId;

    @Id
    @Column(name = "ROLE_ID")
    private int roleId;

    @Relationship(
            columns = @Relationship.RelationshipColumn(sourceColumn = "ROLE_ID", targetColumn = "ROLE_ID")
    )
    private Role role;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
