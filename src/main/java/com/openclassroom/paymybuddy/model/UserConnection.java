package com.openclassroom.paymybuddy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "user_connections")
@IdClass(UserConnectionId.class)
public class UserConnection {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "connection_id", nullable = false)
    private User connection;

    public UserConnection() {
    }

    public UserConnection(User user, User connection) {
        this.user = user;
        this.connection = connection;
    }
}