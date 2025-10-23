package com.example.My_Chat.controller;

import com.example.My_Chat.Config.JwtUtil;
import com.example.My_Chat.DTO.LoginResponse;
import com.example.My_Chat.DTO.RegisterRequest;
import com.example.My_Chat.DTO.UserDTO;
import com.example.My_Chat.model.User;
import com.example.My_Chat.repository.UserRepository;
import com.example.My_Chat.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    static class LoginRequest{
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginRepose(@RequestBody LoginRequest request){
        // FORCE REBUILD 123
       User user = userRepository.findByUsername(request.getUsername());
       if(user == null){
           throw new RuntimeException("not found user");
       }

        //Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) //matches sẽ băm cả mật khẩu nhập vào để so sánh với mật khảu trong
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(" Sai mật khẩu");
        }

        String token = jwtUtil.generateToken(user);

        //Nếu đúng → trả thông tin user
        LoginResponse response = new LoginResponse(user.getUsername(), user.getRole(), token);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/register")
    public ResponseEntity<?>  registerUsers (@RequestBody RegisterRequest req){
      try{
          UserDTO userDto = userService.registerUser(req);
          return ResponseEntity.ok(userDto);
      }catch (Exception e){
          return ResponseEntity.badRequest().body(e.getMessage());
      }
    }



}
