package com.encrypto.EncryptoClient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import javax.crypto.SecretKey;

@Getter
@Setter
@AllArgsConstructor
public class UserWithMessagesDTO {
    private UserDTO user;
    private SecretKey secretKey;
    private List<MessageDTO> messages;
}
