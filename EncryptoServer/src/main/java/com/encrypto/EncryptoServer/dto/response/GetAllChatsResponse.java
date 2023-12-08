package com.encrypto.EncryptoServer.dto.response;

import com.encrypto.EncryptoServer.dto.MessageDTO;

import java.util.List;
import java.util.Map;

public record GetAllChatsResponse(Map<String, List<MessageDTO>> chats) {}
