package com.encrypto.EncryptoClient.service;

import com.encrypto.EncryptoClient.dto.request.GetAllChatsRequest;
import com.encrypto.EncryptoClient.dto.response.GetAllChatsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

public class ChatService {
    private final HttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public ChatService(HttpClient httpClient) {
        this.client = httpClient;
    }

    public GetAllChatsResponse fetchAllChats(String username) {
        try {
            var getAllMessagesRequest = new GetAllChatsRequest(username);
            var reqJson = objectMapper.writeValueAsString(getAllMessagesRequest);
            logger.debug("Get all messages request: {}", reqJson);
            var uri = String.format("http://localhost:8080/api/messages/%s/all", username);
            var req =
                    HttpRequest.newBuilder()
                            .uri(URI.create(uri))
                            .header("Content-Type", "application/json")
                            .GET()
                            .build();
            return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(this::parseGetAllMessagesResponse)
                    .exceptionally(
                            e -> {
                                logger.error("Error fetching all chats", e);
                                return null;
                            })
                    .get();

        } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private GetAllChatsResponse parseGetAllMessagesResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, GetAllChatsResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
