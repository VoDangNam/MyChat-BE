package com.example.My_Chat.controller;
import com.example.My_Chat.Config.JwtUtil;
import com.example.My_Chat.DTO.FriendRequestDTO;
import com.example.My_Chat.DTO.UserDTO;
import com.example.My_Chat.model.Friend;
import com.example.My_Chat.model.User;
import com.example.My_Chat.service.FriendService;
import com.example.My_Chat.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/friends")
public class FriendController {

    private final JwtUtil jwtUtil;
    private final FriendService friendService;
    private final UserRepository userRepository;

    public FriendController(JwtUtil jwtUtil, FriendService friendService, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.friendService = friendService;
        this.userRepository = userRepository;
    }
    private UserDTO convertToUserDTO(User user) {
        if (user == null) return null;
        // Thực hiện chuyển đổi thủ công hoặc dùng thư viện như ModelMapper/MapStruct
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        // KHÔNG BAO GỒM PASSWORD
        return dto;
    }
    @GetMapping("/search")
    public List<UserDTO> searchUsers(@RequestParam String keyword, @RequestHeader("Authorization") String token ) {
        String jwt = token.substring(7);
        String currenUser = jwtUtil.extractUsername(jwt);

        // SỬA: Chuyển đổi List<User> sang List<UserDTO>
        return friendService.searchUsers(keyword, currenUser).stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/sendRequest")
    public ResponseEntity<Friend> sendRequest(@RequestHeader("Authorization") String token, @RequestParam String toUser) {
        String jwt = token.substring(7);
        String fromUser = jwtUtil.extractUsername(jwt);

        User u1 = userRepository.findByUsername(fromUser);
        User u2 = userRepository.findByUsername(toUser);

        if (u1 == null || u2 == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User không tồn tại!");
        }

        try {
            Friend newRequest = friendService.sendFriendRequest(u1.getId(), u2.getId());
            return new ResponseEntity<>(newRequest, HttpStatus.CREATED); // Trả về 201 Created
        } catch (RuntimeException e) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/declineRequest")
    public ResponseEntity<Void> declineRequest(@RequestHeader("Authorization") String token,@RequestParam Long requestId){
        String jwt = token.substring(7);
        String username = jwtUtil.extractUsername(jwt);
        User currentUser = userRepository.findByUsername(username);

        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User không tồn tại.");
        }

        try {
            friendService.declineFriendRequest(requestId, currentUser.getId());
            return ResponseEntity.noContent().build(); // Trả về 204 No Content (Thành công nhưng không có nội dung)
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này."); // Trả về 403 Forbidden
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()); // Các lỗi khác như "Không tồn tại", "Sai trạng thái"
        }
    }


    @PostMapping("/acceptRequest")
    public ResponseEntity<Friend> acceptRequest(@RequestHeader("Authorization") String token,
                                @RequestParam Long requestId) {
        String jwt = token.substring(7);
        String username = jwtUtil.extractUsername(jwt);

        User currentUser = userRepository.findByUsername(username);
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User không tồn tại.");
        }

        try {
            Friend acceptedFriend = friendService.acceptFriendRequest(requestId, currentUser.getId());
            return ResponseEntity.ok(acceptedFriend); // Trả về 200 OK
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền chấp nhận lời mời này.");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    @GetMapping("/M")
    public List<UserDTO> getFriends(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtUtil.extractUsername(jwt);

        User me = userRepository.findByUsername(username);
        if (me == null) return List.of();

        return friendService.getFriends(me.getId()).stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/requests")
    public List<FriendRequestDTO> getPendingRequests(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtUtil.extractUsername(jwt);
        User me = userRepository.findByUsername(username);

        if (me == null) return List.of();

        return friendService.getPendingRequests(me.getId());
    }
}
