package com.encrypto.EncryptoServer.dto.response;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetPublicKeyResponse {
    boolean success;
    String publicKey;
}
