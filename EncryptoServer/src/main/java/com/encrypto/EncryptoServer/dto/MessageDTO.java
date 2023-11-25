package com.encrypto.EncryptoServer.dto;

import lombok.Getter;

import java.time.Instant;

@Getter
public class MessageDTO {
    private String senderId;
    private String receiverId;
    private String content;
    private Instant timestamp;
}
