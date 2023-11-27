package com.encrypto.EncryptoServer.controller;

import static java.util.Base64.getDecoder;

import com.encrypto.EncryptoServer.dto.request.LoginRequest;
import com.encrypto.EncryptoServer.dto.request.UserRequest;
import com.encrypto.EncryptoServer.dto.response.LoginResponse;
import com.encrypto.EncryptoServer.dto.response.RegisterResponse;
import com.encrypto.EncryptoServer.model.Users;
import com.encrypto.EncryptoServer.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthenticationController {
    private final SecurityContextHolderStrategy strategy =
            SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest req) {
        // Log the user request body.
        try {
            var decodedPassword = getDecoder().decode(req.getPassword());
            var user = new Users();

            user.setUsername(req.getUsername());
            user.setDateOfBirth(req.getDateOfBirth());
            user.setPassword(passwordEncoder.encode(new String(decodedPassword)));
            user.setPublicKey(req.getPublicKey());

            userRepository.save(user);

            var res = new RegisterResponse(true, "User registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (DataIntegrityViolationException ex) {
            var res = new RegisterResponse(false, "Username already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody @Valid LoginRequest loginReq,
            HttpServletRequest req,
            HttpServletResponse res) {
        // Log the user request body.
        try {
            var decodedPassword = getDecoder().decode(loginReq.getPassword());
            var auth =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginReq.getUsername(), new String(decodedPassword)));
            var context = strategy.createEmptyContext();
            context.setAuthentication(auth);
            strategy.setContext(context);
            securityContextRepository.saveContext(context, req, res);
            return ResponseEntity.ok(
                    new LoginResponse(loginReq.getUsername(), "Logged in Successfully"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Username/password entered is incorrect or the User does not exist.");
        }
    }
}
