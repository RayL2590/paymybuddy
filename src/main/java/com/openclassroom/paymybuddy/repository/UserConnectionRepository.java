package com.openclassroom.paymybuddy.repository;

import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.model.UserConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {
    List<UserConnection> findByUserId(Long userId);
    boolean existsByUserAndConnection(User user, User connection);
}