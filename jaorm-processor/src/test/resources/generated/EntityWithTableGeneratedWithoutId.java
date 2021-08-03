package io.test;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;
import io.github.ulisse1996.jaorm.annotation.TableGenerated;

@Table(name = "TABLE2")
public class EntityWithTableGeneratedWithoutId {

    @Id
    @Column(name = "COL1")
    private int id;

    @Column(name = "COL2", autoGenerated = true)
    @TableGenerated(tableName = "TAB1", keyColumn = "COL1", valueColumn = "COL", matchKey = "KEY")
    private int gen;

    public int getGen() {
        return gen;
    }

    public void setGen(int gen) {
        this.gen = gen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
