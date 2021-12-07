package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

@Table(name = "USER_SPECIFIC")
public class UserSpecific {

    @Id
    @Column(name = "USER_ID")
    private int userId;

    @Column(name = "SPECIFIC_ID")
    private int specificId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSpecificId() {
        return specificId;
    }

    public void setSpecificId(int specificId) {
        this.specificId = specificId;
    }

    @Override
    public String toString() {
        return "UserSpecific{" +
                "userId=" + userId +
                ", specificId=" + specificId +
                '}';
    }
}
