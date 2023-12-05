package com.encrypto.EncryptoClient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserWithMessagesDTO {
    private UserDTO user;
    private List<MessageDTO> messages;
}
