package com.encrypto.EncryptoServer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;

import java.util.Date;

@Getter
public class UserRequest {
    @NotBlank(message = "Username must be blank")
    private String username;

    @NotBlank(message = "Password must not be blank")
    private String password;

    @NotNull(message = "DateOfBirth is required")
    private Date dateOfBirth;

    @NotBlank(message = "Public Key must not be blank")
    private String publicKey;
}
