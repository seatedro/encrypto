package com.encrypto.EncryptoClient.util;


import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.net.CookieManager;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutionException;

public class StompClient {
    private WebSocketStompClient stompClient;
    private final String url = "ws://localhost:8080/encrypto";
    private final HttpClient httpClient;

    public StompClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    private WebSocketHttpHeaders createConnectHeaders() {
        var headers = new WebSocketHttpHeaders();

        var cookieManager =
                (CookieManager)
                        httpClient
                                .cookieHandler()
                                .orElseThrow(
                                        () -> new IllegalStateException("CookieManager not set!"));
        var cookieStore = cookieManager.getCookieStore();
        var cookies = cookieStore.getCookies();

        var cookieHeader = new StringBuilder();
        for (var cookie : cookies) {
            if (!cookieHeader.isEmpty()) {
                cookieHeader.append("; ");
            }
            cookieHeader.append(cookie.getName()).append("=").append(cookie.getValue());
        }

        if (!cookieHeader.isEmpty()) {
            headers.add("Cookie", cookieHeader.toString());
        }

        return headers;
    }

    public StompSession connect() {
        var sessionHandler = new MyStompSessionHandler();
        try {
            return stompClient.connectAsync(url, createConnectHeaders(), sessionHandler).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to Encrypto STOMP session!");
            super.afterConnected(session, connectedHeaders);
        }
    }
}
