package io.jaorm.integration.test.entity;

import io.jaorm.annotation.Column;
import io.jaorm.annotation.Id;
import io.jaorm.annotation.Relationship;
import io.jaorm.annotation.Table;

import java.util.List;
import java.util.Objects;

@Table(name = "USER_ENTITY")
public class User {

    @Id
    @Column(name = "USER_ID")
    private int id;

    @Column(name = "USER_NAME")
    private String name;

    @Column(name = "DEPARTMENT_ID")
    private int departmentId;

    @Relationship(
            columns = @Relationship.RelationshipColumn(targetColumn = "USER_ID", sourceColumn = "USER_ID")
    )
    private List<UserRole> roles;

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
        return id == user.id && departmentId == user.departmentId && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, departmentId);
    }
}
