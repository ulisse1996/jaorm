package io.github.ulisse1996.jaorm.integration.test.projection;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Projection;

import java.time.LocalDate;

@Projection
public class DateProjection {

    @Column(name = "MY_DATE")
    private LocalDate myDate;

    public LocalDate getMyDate() {
        return myDate;
    }

    public void setMyDate(LocalDate myDate) {
        this.myDate = myDate;
    }
}
