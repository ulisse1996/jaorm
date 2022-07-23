package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Table(name = "SCHOOL")
public class School {

    @Id
    @Column(name = "SCHOOL_ID")
    private BigDecimal schoolId;

    @Column(name = "NAME")
    private String name;

    @Cascade(CascadeType.ALL)
    @Relationship(columns = {
            @Relationship.RelationshipColumn(targetColumn = "SCHOOL_ID", sourceColumn = "SCHOOL_ID"),
            @Relationship.RelationshipColumn(targetColumn = "NAME", defaultValue = "MY_NAME")
    })
    private List<Student> students;

    @Cascade(CascadeType.ALL)
    @Relationship(columns = {
            @Relationship.RelationshipColumn(targetColumn = "SCHOOL_ID", sourceColumn = "SCHOOL_ID")
    })
    private List<Student> allStudents;

    public List<Student> getAllStudents() {
        return allStudents;
    }

    public void setAllStudents(List<Student> allStudents) {
        this.allStudents = allStudents;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public BigDecimal getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(BigDecimal schoolId) {
        this.schoolId = schoolId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
