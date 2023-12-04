package com.encrypto.EncryptoServer.dto.response;

import java.util.Set;

public record GetAllChatsResponse(Set<String> usernames) {}
