package io.github.ulisse1996.jaorm.integration.test.projection;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Projection;

import java.sql.Timestamp;

@Projection
public class TimestampProjection {

    @Column(name = "MY_TIMESTAMP")
    private Timestamp myTimestamp;

    public Timestamp getMyTimestamp() {
        return myTimestamp;
    }

    public void setMyTimestamp(Timestamp myTimestamp) {
        this.myTimestamp = myTimestamp;
    }
}
