package com.encrypto.EncryptoClient.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private String senderId;
    private String receiverId;
    private String content;
    private Instant timestamp;
}
