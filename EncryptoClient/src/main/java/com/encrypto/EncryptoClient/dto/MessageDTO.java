package com.encrypto.EncryptoClient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private String senderId;
    private String receiverId;
    private String content;
    private String timestamp;
}
