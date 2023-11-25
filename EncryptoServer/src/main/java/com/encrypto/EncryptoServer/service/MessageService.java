package com.encrypto.EncryptoServer.service;

import com.encrypto.EncryptoServer.dto.MessageDTO;
import com.encrypto.EncryptoServer.model.Messages;
import com.encrypto.EncryptoServer.repository.MessageRepository;
import com.encrypto.EncryptoServer.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository userRepository;

    public Messages sendMessage(MessageDTO messageDTO) {
        var message = new Messages();
        var sender =
                userRepository
                        .findByUsername(messageDTO.getSenderId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid sender id."));
        var receiver =
                userRepository
                        .findByUsername(messageDTO.getReceiverId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid receiver id."));
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(messageDTO.getContent());
        message.setTimestamp(messageDTO.getTimestamp());
        return messageRepository.save(message);
    }
}
