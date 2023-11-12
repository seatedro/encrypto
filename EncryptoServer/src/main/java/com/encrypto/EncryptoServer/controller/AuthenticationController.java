package com.encrypto.EncryptoServer.controller;

import com.encrypto.EncryptoServer.dto.response.RegisterResponse;
import com.encrypto.EncryptoServer.model.Users;
import com.encrypto.EncryptoServer.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthenticationController {
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> signUp(@Valid @RequestBody Users user) {
        // Log the user request body.
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            RegisterResponse res = new RegisterResponse(true, "User registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (DataIntegrityViolationException ex) {
            RegisterResponse res = new RegisterResponse(false, "Username already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }
}
