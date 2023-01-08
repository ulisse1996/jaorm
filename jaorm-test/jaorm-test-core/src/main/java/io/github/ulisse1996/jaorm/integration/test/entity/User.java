package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.*;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.graph.EntityGraphFetcher;
import io.github.ulisse1996.jaorm.mapping.Cursor;

import java.util.List;
import java.util.Objects;

@Table(name = "USER_ENTITY")
@Graph(name = "UserFull", nodes = {"roles", "userSpecific"})
@Graph(name = "UserFullWithRoles", nodes = {"roles", "userSpecific"}, subGraphs = {"withRole"})
public class User {

    public static final EntityGraphFetcher<User> USER_FULL = EntityGraphFetcher.of(User.class, "UserFull");
    public static final EntityGraphFetcher<User> USER_FULL_WITH_ROLES = EntityGraphFetcher.of(User.class, "UserFullWithRoles");

    @Id
    @Column(name = "USER_ID")
    private int id;

    @Column(name = "USER_NAME")
    private String name;

    @Column(name = "DEPARTMENT_ID")
    private int departmentId;

    @Cascade(CascadeType.ALL)
    @Relationship(
            columns = @Relationship.RelationshipColumn(targetColumn = "USER_ID", sourceColumn = "USER_ID")
    )
    private List<UserRole> roles;

    @Cascade(CascadeType.ALL)
    @Relationship(
            columns = @Relationship.RelationshipColumn(targetColumn = "USER_ID", sourceColumn = "USER_ID")
    )
    private Cursor<UserRole> rolesCursor;

    @Cascade(CascadeType.ALL)
    @Relationship(
            columns = @Relationship.RelationshipColumn(targetColumn = "USER_ID", sourceColumn = "USER_ID")
    )
    private Result<UserSpecific> userSpecific;

    public Cursor<UserRole> getRolesCursor() {
        return rolesCursor;
    }

    public void setRolesCursor(Cursor<UserRole> rolesCursor) {
        this.rolesCursor = rolesCursor;
    }

    public Result<UserSpecific> getUserSpecific() {
        return userSpecific;
    }

    public void setUserSpecific(Result<UserSpecific> userSpecific) {
        this.userSpecific = userSpecific;
    }

    public List<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(List<UserRole> roles) {
        this.roles = roles;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && departmentId == user.departmentId && Objects.equals(name, user.name) && Objects.equals(roles, user.roles) && Objects.equals(rolesCursor, user.rolesCursor) && Objects.equals(userSpecific, user.userSpecific);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, departmentId, roles, rolesCursor, userSpecific);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", departmentId=" + departmentId +
                ", roles=" + roles +
                ", rolesCursor=" + rolesCursor +
                ", userSpecific=" + userSpecific +
                '}';
    }
}
