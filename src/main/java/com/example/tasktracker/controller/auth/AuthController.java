package com.example.tasktracker.controller.auth;

import com.example.tasktracker.model.User;
import com.example.tasktracker.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Key jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        Optional<User> existingUser = userRepo.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginReq) {
        Optional<User> userOpt = userRepo.findByUsername(loginReq.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid username");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(loginReq.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid password");
        }

        String jwt = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(jwtSecretKey)
                .compact();

        return ResponseEntity.ok(jwt);
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        Optional<User> userOpt = userRepo.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);

        return ResponseEntity.ok("Password updated successfully");
    }

}
