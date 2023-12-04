package com.encrypto.EncryptoClient.util;

import com.encrypto.EncryptoClient.dto.request.MessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompSession;

import java.net.http.HttpClient;
import java.time.Instant;

public class StompSessionManager {
    private static final Logger logger = LoggerFactory.getLogger(StompSessionManager.class);
    @Setter private StompSession socket;
    @Setter private StompClient stompClient;
    @Getter @Setter private boolean isConnected = false;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StompSessionManager(HttpClient httpClient) {
        setStompClient(new StompClient(httpClient));
    }

    public void connect() {
        setSocket(stompClient.connect());
        setConnected(true);
    }

    public void disconnect() {
        if (isConnected && socket != null) {
            socket.disconnect();
            setConnected(false);
        }
    }

    public void sendMessage(String senderId, String receiverId, String content, Instant timestamp)
            throws IllegalStateException {
        if (!isConnected || socket == null)
            throw new IllegalStateException("Cannot send message when session is not connected");
        try {
            var messageReq = new MessageDTO(senderId, receiverId, content, timestamp);
            var messageReqJson = objectMapper.writeValueAsString(messageReq);
            logger.info("Message request: {}", messageReqJson);
            socket.send("/app/send", messageReqJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void subscribe(String destination, StompFrameHandler fn) throws IllegalStateException {
        if (!isConnected || socket == null)
            throw new IllegalStateException("Cannot subscribe when session is not connected");
        socket.subscribe(destination, fn);
    }
}
