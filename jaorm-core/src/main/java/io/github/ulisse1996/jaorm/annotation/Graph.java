package io.github.ulisse1996.jaorm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(Graph.Graphs.class)
public @interface Graph {

    String name();
    String[] nodes();
    String[] subGraphs() default {};

    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.TYPE)
    @interface Graphs {

        Graph[] value();
    }
}
