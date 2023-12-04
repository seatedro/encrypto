package com.encrypto.EncryptoClient.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public record GetPublicKeyResponse(String publicKey) {}
