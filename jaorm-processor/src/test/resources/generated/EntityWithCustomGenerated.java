package io.test;

import io.github.ulisse1996.jaorm.annotation.*;

@Table(name = "TABLE")
public class EntityWithCustomGenerated {

    @Id(autoGenerated = true)
    @Column(name = "COL1")
    @CustomGenerated(MyCustomGenerator.class)
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static class MyCustomGenerator implements CustomGenerator<Integer> {

        public Integer generate(Class<?> entityClass, Class<?> columnClass, String columnName) {
            return 1;
        }
    }
}
