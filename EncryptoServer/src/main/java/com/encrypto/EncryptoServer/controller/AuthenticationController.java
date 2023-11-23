package com.encrypto.EncryptoServer.controller;

import com.encrypto.EncryptoServer.dto.response.LoginResponse;
import com.encrypto.EncryptoServer.dto.response.RegisterResponse;
import com.encrypto.EncryptoServer.model.Users;
import com.encrypto.EncryptoServer.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthenticationController {
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @CrossOrigin(origins = "*")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody Users user) {
        // Log the user request body.
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            var res = new RegisterResponse(true, "User registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (DataIntegrityViolationException ex) {
            var res = new RegisterResponse(false, "Username already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @CrossOrigin(origins = "http://localhost:8081")
    @GetMapping("/login")
    public ResponseEntity<?> login() {
        // Log the user request body.
        try {
            return ResponseEntity.ok(new LoginResponse("Succesfully Logged in."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }
    }
}
