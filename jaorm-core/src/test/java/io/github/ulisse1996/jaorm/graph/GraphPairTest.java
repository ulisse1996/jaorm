package io.github.ulisse1996.jaorm.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GraphPairTest {

    @Test
    void should_return_true_for_equals_instance() {
        GraphPair pair = new GraphPair(Object.class, "NAME");
        GraphPair pair2 = new GraphPair(Object.class, "NAME");

        Assertions.assertEquals(pair, pair2);
        Assertions.assertEquals(pair.toString(), pair2.toString());
    }
}
