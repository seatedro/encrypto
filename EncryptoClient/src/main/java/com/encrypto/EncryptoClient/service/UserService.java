package com.encrypto.EncryptoClient.service;

import com.encrypto.EncryptoClient.dto.response.GetPublicKeyResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UserService {
    private final HttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(HttpClient httpClient) {
        this.client = httpClient;
    }

    public GetPublicKeyResponse getPublicKey(String username) {
        try {
            var uri = String.format("http://localhost:8080/api/users/%s/public_key", username);
            var req =
                    HttpRequest.newBuilder()
                            .uri(URI.create(uri))
                            .header("Content-Type", "application/json")
                            .GET()
                            .build();
            var res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (HttpStatus.valueOf(res.statusCode()).is2xxSuccessful()) {
                return parseGetPublicKeyResponse(res.body());
            }
            logger.error("Failed to get public key: {}, {}", res.statusCode(), res.body());
            return null;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private GetPublicKeyResponse parseGetPublicKeyResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, GetPublicKeyResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
