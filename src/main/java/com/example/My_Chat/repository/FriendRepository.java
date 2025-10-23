package com.example.My_Chat.repository;

import com.example.My_Chat.model.Friend;
import com.example.My_Chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend,Long> {
    @Query("SELECT u FROM User u WHERE u.id IN " +
            "(SELECT f.friendId FROM Friend f WHERE f.userId = :userId AND f.status = 'accepted')")
    List<User> findFriendsByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Friend f JOIN FETCH f.user WHERE f.friendId = ?1 AND f.status = 'pending'")
    List<Friend> findPendingRequests(@Param("userId") Long userId);

    List<Friend> findByUserIdOrFriendId(Long userId, Long friendId);
    List<Friend> findByFriendIdAndStatus(Long friendId, String status);
    Friend findByUserIdAndFriendId(Long userId, Long friendId);
}
