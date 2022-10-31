package io.github.ulisse1996.jaorm.integration.test.projection;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Projection;

@Projection
public class UsernameProjection {

    @Column(name = "USER_NAME")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
