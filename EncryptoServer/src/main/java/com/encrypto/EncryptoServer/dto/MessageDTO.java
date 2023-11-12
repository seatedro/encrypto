package com.encrypto.EncryptoServer.dto;

import lombok.Getter;

import java.time.Instant;

@Getter
public class MessageDTO {
    private Long senderId;
    private Long receiverId;
    private String content;
    private Instant timestamp;
}
