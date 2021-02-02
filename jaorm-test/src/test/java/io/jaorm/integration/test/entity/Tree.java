package io.jaorm.integration.test.entity;

import io.jaorm.processor.annotation.Column;
import io.jaorm.processor.annotation.Id;
import io.jaorm.processor.annotation.Table;

import java.util.Objects;

@Table(name = "TREE_ENTITY")
public class Tree {

    @Id
    @Column(name = "TREE_ID")
    private int id;

    @Column(name = "TREE_NAME")
    private String name;

    @Column(name = "FRUIT_ID")
    private int fruitId;

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

    public int getFruitId() {
        return fruitId;
    }

    public void setFruitId(int fruitId) {
        this.fruitId = fruitId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tree tree = (Tree) o;
        return id == tree.id && fruitId == tree.fruitId && Objects.equals(name, tree.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fruitId);
    }
}
