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

public class UserService {
    private final HttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(HttpClient httpClient) {
        this.client = httpClient;
    }
}
