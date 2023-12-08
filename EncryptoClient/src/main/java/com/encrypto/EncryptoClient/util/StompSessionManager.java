package com.encrypto.EncryptoClient.util;

import com.encrypto.EncryptoClient.dto.MessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
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

    public void sendMessage(String senderId, String receiverId, String content, Instant timestamp) {
        if (!isConnected || socket == null)
            throw new IllegalStateException("Cannot send message when session is not connected");
        var messageReq = new MessageDTO(senderId, receiverId, content, timestamp);
        socket.send("/app/send", messageReq);
    }

    public void subscribe(String destination, StompFrameHandler fn) throws IllegalStateException {
        if (!isConnected || socket == null)
            throw new IllegalStateException("Cannot subscribe when session is not connected");
        socket.subscribe(destination, fn);
    }
}
