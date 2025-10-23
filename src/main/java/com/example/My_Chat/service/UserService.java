package com.example.My_Chat.service;

import com.example.My_Chat.DTO.RegisterRequest;
import com.example.My_Chat.DTO.UserDTO;
import com.example.My_Chat.model.User;
import com.example.My_Chat.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;
@Service
public class UserService {

    private final PasswordEncoder hashP;

   private final UserRepository userRepository;

    public UserService(PasswordEncoder hashP, UserRepository userRepository) {
        this.hashP = hashP;
        this.userRepository = userRepository;
    }


    public UserDTO registerUser(RegisterRequest req){
        User newUser = new User();
       UserDTO dto = new UserDTO();

        if(Stream.of(req.getUsername(), req.getEmail(),req.getPassword(),req.getConfirmPassword()).anyMatch(s -> s==null || s.trim().isEmpty())){
            throw new RuntimeException("Vui Lòng nhập đầy đủ");
        }
        if(userRepository.findByUsername(req.getUsername())!=null){
            throw new RuntimeException("Tên "+req.getUsername()+" đã tồn tại vui lòng nhập tên mới");
        }
        if (userRepository.findByEmail(req.getEmail()) != null) {
            throw new RuntimeException("Email " + req.getEmail() + " đã được sử dụng!");
        }

        if(!req.getConfirmPassword().equals(req.getPassword())){
            throw new RuntimeException("Mật khaảu không trùng khớp");
        }
        String pass = hashP.encode(req.getPassword());
        newUser.setUsername(req.getUsername());
        newUser.setEmail(req.getEmail());
        newUser.setRole("USER");
        newUser.setPassword(pass);
        User savedUser = userRepository.save(newUser);

        dto.setId(savedUser.getId());
        dto.setUsername(savedUser.getUsername());
        dto.setEmail(savedUser.getEmail());
        dto.setRole(savedUser.getRole());
        return dto;
    }
}
