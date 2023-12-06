package com.encrypto.EncryptoServer.controller;

import com.encrypto.EncryptoServer.dto.response.GetPublicKeyResponse;
import com.encrypto.EncryptoServer.service.CustomUserDetailsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UsersController {
    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);
    @Autowired private CustomUserDetailsService userService;

    @GetMapping("/{username}/public_key")
    public ResponseEntity<?> getPublicKey(@PathVariable String username) {
        try {
            logger.info("Getting public key for user: " + username);
            var publicKey = userService.findByUsername(username).getPublicKey();
            return ResponseEntity.ok(new GetPublicKeyResponse(true, publicKey));
        } catch (UsernameNotFoundException e) {
            logger.error("Error getting public key for user: " + username, e);
            return ResponseEntity.notFound().build();
        }
    }
}
