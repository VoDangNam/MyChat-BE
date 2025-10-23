package com.example.My_Chat.service;

import com.example.My_Chat.DTO.FriendRequestDTO;
import com.example.My_Chat.DTO.UserDTO;
import com.example.My_Chat.model.Friend;
import com.example.My_Chat.model.User;
import com.example.My_Chat.repository.FriendRepository;
import com.example.My_Chat.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public FriendService(FriendRepository friendRepository, UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
    }

    public List<User> searchUsers(String keyword, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername);
        if(currentUser == null){
            throw new RuntimeException("not found user");
        }
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(keyword).stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .collect(Collectors.toList());

        List<Friend> relations =friendRepository.findByUserIdOrFriendId(currentUser.getId(),currentUser.getId());

        Set<Long> relatedIds = relations.stream()
                .filter(f -> f.getStatus().equalsIgnoreCase("PENDING")
                ||f.getStatus().equalsIgnoreCase("ACCEPTED"))
                .map(f ->{
                    if(f.getUserId().equals(currentUser.getId())){
                        return f.getFriendId();
                    }
                    else{
                        return f.getUserId();
                    }
                })
                .collect(Collectors.toSet());

        return users.stream()
                .filter(u -> !relatedIds.contains(u.getId()))
                .collect(Collectors.toList());

    }


    public Friend sendFriendRequest(Long userId, Long friendId) {
        if (friendRepository.findByUserIdAndFriendId(userId, friendId) != null ||
                friendRepository.findByUserIdAndFriendId(friendId, userId) != null) {

            throw new RuntimeException("Đã gửi lời mời, đã nhận lời mời, hoặc đã là bạn bè!");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User gửi không tồn tại."));
        User friendUser = userRepository.findById(friendId).orElseThrow(() -> new RuntimeException("User nhận không tồn tại."));

        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus("pending");
        return friendRepository.save(friend);
    }


    // Trong FriendService.java

    public Friend acceptFriendRequest(Long requestId, Long currentUserId) {

        // 1. Tìm lời mời kết bạn
        Friend request = friendRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời!"));

        // 2. KIỂM TRA QUYỀN (ỦY QUYỀN - Authorization)
        // Đảm bảo người dùng hiện tại (currentUserId) chính là người nhận lời mời.
        // Dùng getUserId() và getFriendId() như trong Entity của bạn.
        // Bạn giả định rằng 'friendId' là người nhận (ToUser).

        if (!request.getFriendId().equals(currentUserId)) {
            throw new SecurityException("Bạn không có quyền chấp nhận lời mời này.");
        }

        // 3. Cập nhật trạng thái của lời mời gốc
        request.setStatus("accepted");
        friendRepository.save(request);

        // 4. THÊM DÒNG NGƯỢC LẠI (Tạo mối quan hệ hai chiều)

        // Kiểm tra xem bản ghi ngược lại đã tồn tại chưa (tránh trùng lặp)
        // Bản ghi ngược lại sẽ là: UserId = Người nhận (currentUserId), FriendId = Người gửi (request.getUserId())
        if (friendRepository.findByUserIdAndFriendId(request.getFriendId(), request.getUserId()) == null) {

            Friend reverse = new Friend();
            // Lật ngược ID: người nhận lời mời ban đầu (f.getFriendId) trở thành người gửi (reverse.setUserId)
            reverse.setUserId(request.getFriendId());
            // người gửi lời mời ban đầu (f.getUserId) trở thành người nhận (reverse.setFriendId)
            reverse.setFriendId(request.getUserId());
            reverse.setStatus("accepted");

            friendRepository.save(reverse);
        }

        return request;
    }


    public List<User> getFriends(Long userId) {
        return friendRepository.findFriendsByUserId(userId);
    }

    public List<FriendRequestDTO> getPendingRequests(Long userId) {
        List<Friend> requests = friendRepository.findPendingRequests(userId);

        return requests.stream().map(req -> {
            FriendRequestDTO dto = new FriendRequestDTO();
            dto.setRequestId(req.getId());

            // Dòng này cần đảm bảo User đã được tải (Tải Eagerly, Fetch Join, hoặc @Transactional)
            dto.setFromUsername(req.getUser().getUsername());
            dto.setAvatar(req.getUser().getAvt());
            return dto;
        }).toList();
    }

    public void declineFriendRequest(Long requestId, Long currentUserId){
        // 1. Tìm bản ghi lời mời kết bạn
        Friend friendRequest = friendRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Lời mời kết bạn không tồn tại."));

        // 2. KIỂM TRA QUYỀN (Ủy quyền)
        // Lớp Friend của bạn đặt tên 'friend' là Người nhận lời mời.
        // friendRequest.getFriend() sẽ trả về đối tượng User của người nhận.

        if (friendRequest.getFriend() == null) {
            throw new RuntimeException("Thông tin người nhận bị thiếu trong lời mời.");
        }

        // So sánh ID của Người nhận lời mời (friendRequest.getFriend().getId())
        // với ID của Người dùng hiện tại (currentUserId)
        if (!friendRequest.getFriend().getId().equals(currentUserId)) {
            // Nếu không khớp, ném lỗi bảo mật
            throw new SecurityException("Bạn không có quyền từ chối lời mời này.");
        }

        // 3. Thực hiện từ chối bằng cách xóa bản ghi
        // Chỉ xóa nếu status đang là 'pending' (tùy chọn, nên kiểm tra)
        if ("pending".equalsIgnoreCase(friendRequest.getStatus())) {
            friendRepository.delete(friendRequest);
        } else {
            throw new IllegalStateException("Không thể từ chối lời mời không còn ở trạng thái chờ.");
        }
    }




}
