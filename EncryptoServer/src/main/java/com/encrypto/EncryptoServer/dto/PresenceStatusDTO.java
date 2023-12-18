package com.encrypto.EncryptoServer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PresenceStatusDTO {
    private String userId;
    private boolean isOnline;
}
