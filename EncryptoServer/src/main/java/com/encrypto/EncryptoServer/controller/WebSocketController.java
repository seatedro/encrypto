package com.encrypto.EncryptoServer.controller;

import com.encrypto.EncryptoServer.dto.MessageDTO;
import com.encrypto.EncryptoServer.dto.PresenceStatusDTO;
import com.encrypto.EncryptoServer.service.MessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    @Autowired private MessageService messageService;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    public void sendPrivateMessage(@Payload MessageDTO messageDTO) {
        var message = messageService.sendMessage(messageDTO);
        messagingTemplate.convertAndSendToUser(messageDTO.getReceiverId(), "/private", messageDTO);
    }

    @MessageMapping("/presence")
    @SendTo("/topic/presence")
    public PresenceStatusDTO sendPresenceStatus(@Payload PresenceStatusDTO presenceStatusDTO) {
        return presenceStatusDTO;
    }
}
