package com.example.My_Chat.repository;

import com.example.My_Chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsernameAndPassword(String username,String password);
    User findByUsername(String username);
    List<User> findByUsernameContainingIgnoreCase(String username);
    User findByEmail(String email);
}
