
package io.github.ulisse1996.jaorm.tools.service.impl;

import io.github.ulisse1996.jaorm.annotation.Query;

public interface MyClass {

    @Query(sql = "SELECT * FROM TAB1", noArgs = true)
    Integer inter();
}
