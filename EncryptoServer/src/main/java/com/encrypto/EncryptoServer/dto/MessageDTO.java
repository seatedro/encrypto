package com.encrypto.EncryptoServer.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String senderId;
    private String receiverId;
    private String content;
    private Instant timestamp;
}
