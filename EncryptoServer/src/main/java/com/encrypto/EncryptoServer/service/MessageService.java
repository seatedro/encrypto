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

    public Map<String, List<MessageDTO>> getAllChats(String username) {
        var sender = userService.findByUsername(username);
        var messages =
                messageRepository.findAllBySenderOrReceiverId(sender).orElse(new ArrayList<>());
        var users = new HashMap<String, List<MessageDTO>>();
        for (var message : messages) {
            String user;
            if (Objects.equals(message.getSender().getId(), sender.getId())) {
                user = message.getReceiver().getUsername();
            } else {
                user = message.getSender().getUsername();
            }
            var chat = users.getOrDefault(user, new ArrayList<>());
            var messageDTO = createMessageDTO(message);
            chat.add(messageDTO);
            users.put(user, chat);
        }
        return users;
    }

    private MessageDTO createMessageDTO(Messages message) {
        var messageDTO = new MessageDTO();
        messageDTO.setSenderId(message.getSender().getUsername());
        messageDTO.setReceiverId(message.getReceiver().getUsername());
        messageDTO.setContent(message.getContent());
        messageDTO.setTimestamp(message.getTimestamp());
        return messageDTO;
    }
}
