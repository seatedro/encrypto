package com.encrypto.EncryptoClient.service;

import com.encrypto.EncryptoClient.dto.response.GetPublicKeyResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

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
            return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(this::parseGetPublicKeyResponse)
                    .exceptionally(
                            e -> {
                                logger.error("Error fetching public key", e);
                                return null;
                            })
                    .get();

        } catch (InterruptedException | ExecutionException e) {
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
