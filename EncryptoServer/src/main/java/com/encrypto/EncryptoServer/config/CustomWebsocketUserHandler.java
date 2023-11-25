package com.encrypto.EncryptoServer.config;

import com.encrypto.EncryptoServer.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

class IdPrincipal implements Principal {
    String id;

    IdPrincipal(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return id;
    }
}

@Component
public class CustomWebsocketUserHandler extends DefaultHandshakeHandler {
    @Autowired private UserRepository userRepository;

    @Override
    protected Principal determineUser(
            ServerHttpRequest req, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        var p = req.getPrincipal();
        var username = p != null ? p.getName() : null;
        System.out.println("Client connected with username: " + username);
        var data = userRepository.findByUsername(username);
        if (data.isPresent()) {
            var user = data.get();
            return new IdPrincipal(user.getId().toString());
        }
        return super.determineUser(req, wsHandler, attributes);
    }
}
