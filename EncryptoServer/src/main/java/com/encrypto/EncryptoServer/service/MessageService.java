package com.encrypto.EncryptoServer.service;

import com.encrypto.EncryptoServer.dto.MessageDTO;
import com.encrypto.EncryptoServer.model.Messages;
import com.encrypto.EncryptoServer.repository.MessageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MessageService {
    @Autowired private MessageRepository messageRepository;
    @Autowired private CustomUserDetailsService userService;

    public Messages sendMessage(MessageDTO messageDTO) {
        var message = new Messages();
        var sender = userService.findByUsername(messageDTO.getSenderId());
        var receiver = userService.findByUsername(messageDTO.getReceiverId());
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(messageDTO.getContent());
        message.setTimestamp(messageDTO.getTimestamp());
        return messageRepository.save(message);
    }

    public Set<String> getAllChats(String username) {
        var sender = userService.findByUsername(username);
        var messages =
                messageRepository.findAllBySenderOrReceiverId(sender).orElse(new ArrayList<>());
        var users = new HashSet<String>();
        for (var message : messages) {
            if (Objects.equals(message.getSender().getId(), sender.getId())) {
                users.add(message.getReceiver().getUsername());
            } else {
                users.add(message.getSender().getUsername());
            }
        }
        return users;
    }
}
