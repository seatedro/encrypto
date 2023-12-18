package com.encrypto.EncryptoServer.controller;

import com.encrypto.EncryptoServer.dto.response.GetAllChatsResponse;
import com.encrypto.EncryptoServer.service.MessageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessagesController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);
    @Autowired private MessageService messageService;

    @GetMapping("/{username}/all")
    public ResponseEntity<?> getAllChats(@PathVariable String username) {
        var chats = messageService.getAllChats(username);
        return ResponseEntity.ok().body(new GetAllChatsResponse(chats));
    }
}
