package com.encrypto.EncryptoClient.components;

import static com.encrypto.EncryptoClient.EncryptoClient.client;

import com.encrypto.EncryptoClient.dto.request.LoginRequest;
import com.encrypto.EncryptoClient.dto.response.LoginResponse;
import com.encrypto.EncryptoClient.elements.PlaceholderPasswordField;
import com.encrypto.EncryptoClient.elements.PlaceholderTextField;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class LoginSignupPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(LoginSignupPanel.class);
    private static JTextField usernameField;
    private static JPasswordField passwordField;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Runnable onSuccessfulLogin;

    public LoginSignupPanel(Runnable onSuccessfulLogin) {
        this.onSuccessfulLogin = onSuccessfulLogin;
        setLayout(new MigLayout("insets 30, fill", "", "[]25[]"));

        var titleLabel = new JLabel("Sign in to App");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, "align center, span, wrap");

        usernameField = new PlaceholderTextField("Username");
        passwordField = new PlaceholderPasswordField("Password");

        add(usernameField, "align center, h 35!, w 300!, span");
        add(passwordField, "align center, h 35!, w 300!, span");

        usernameField.getDocument().putProperty("owner", usernameField);
        passwordField.getDocument().putProperty("owner", passwordField);
        passwordField.getDocument().putProperty("isPassword", true);

        var loginButton = new JButton("Login");
        var signupButton = new JButton("Signup");

        add(loginButton, "align center, h 35!, w 100!, span");
        add(signupButton, "align center, h 35!, w 100!, span");

        loginButton.addActionListener(this::login);
        signupButton.addActionListener(this::signup);

        setFocusable(true);
        addAncestorListener(
                new AncestorListener() {
                    @Override
                    public void ancestorAdded(AncestorEvent event) {
                        LoginSignupPanel.this.requestFocusInWindow();
                    }

                    @Override
                    public void ancestorRemoved(AncestorEvent event) {}

                    @Override
                    public void ancestorMoved(AncestorEvent event) {}
                });
    }

    private void login(ActionEvent e) {
        var username = usernameField.getText();
        var password = passwordField.getPassword();
        logger.info("Login attempt with username: {}", username);
        authenticate(username, password);
    }

    private void signup(ActionEvent e) {
        var username = usernameField.getText();
        var password = passwordField.getPassword();
        logger.info("Register attempt with username: {}", username);
    }

    private void signUp(String username, char[] password, )
    private void authenticate(String username, char[] password) {
        try {
            var loginReq = new LoginRequest(username, new String(password));
            var loginReqJson = objectMapper.writeValueAsString(loginReq);
            logger.info("Login request: {}", loginReqJson);

            var req =
                    HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/auth/login"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(loginReqJson))
                            .build();

            client.sendAsync(req, BodyHandlers.ofString())
                    .thenApply(
                            res -> {
                                logger.info("Login response: {}", res.body());
                                logger.info(
                                        "Login Cookie: {}", res.headers().map().get("Set-Cookie"));
                                return res;
                            })
                    .thenApply(HttpResponse::body)
                    .thenApply(this::parseLoginResponse)
                    .thenAccept(this::handleLoginResponse)
                    .exceptionally(
                            e -> {
                                logger.error("Error authenticating", e);
                                showErrorMessage("Login Failed: " + e.getMessage());
                                return null;
                            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private LoginResponse parseLoginResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, LoginResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleLoginResponse(LoginResponse response) {
        SwingUtilities.invokeLater(
                () -> {
                    if (response == null) {
                        showErrorMessage("Failed to parse server response or communication error.");
                        return;
                    }

                    onSuccessfulLogin.run();
                });
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
