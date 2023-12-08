package com.encrypto.EncryptoClient.dto.response;

import com.encrypto.EncryptoClient.dto.MessageDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetAllChatsResponse {
    private Map<String, List<MessageDTO>> chats;
}
