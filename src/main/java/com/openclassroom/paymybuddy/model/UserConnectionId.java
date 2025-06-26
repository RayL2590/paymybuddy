package com.openclassroom.paymybuddy.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
public class UserConnectionId implements Serializable {
    private Long user;
    private Long connection;

    public UserConnectionId() {
    }

    public UserConnectionId(Long user, Long connection) {
        this.user = user;
        this.connection = connection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserConnectionId that)) return false;
        return Objects.equals(user, that.user) &&
                Objects.equals(connection, that.connection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, connection);
    }
}