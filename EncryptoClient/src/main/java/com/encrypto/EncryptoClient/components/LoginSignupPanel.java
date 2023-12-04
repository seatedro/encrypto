package com.encrypto.EncryptoClient.components;

import static com.encrypto.EncryptoClient.EncryptoClient.client;
import static com.encrypto.EncryptoClient.util.KeyUtils.*;

import com.encrypto.EncryptoClient.EncryptoClient;
import com.encrypto.EncryptoClient.dto.request.LoginRequest;
import com.encrypto.EncryptoClient.dto.request.RegisterRequest;
import com.encrypto.EncryptoClient.dto.response.LoginResponse;
import com.encrypto.EncryptoClient.dto.response.RegisterResponse;
import com.encrypto.EncryptoClient.elements.PlaceholderPasswordField;
import com.encrypto.EncryptoClient.elements.PlaceholderTextField;
import com.encrypto.EncryptoClient.util.StompSessionManager;
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
import java.util.Date;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class LoginSignupPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(LoginSignupPanel.class);
    private static JTextField usernameField;
    private static JPasswordField passwordField;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Runnable onSuccessfulLogin;
    private final JLabel successMessageLabel;
    private final EncryptoClient parent;

    public LoginSignupPanel(EncryptoClient parent) {
        this.parent = parent;
        this.onSuccessfulLogin = parent::transitionToChatPanel;
        setLayout(new MigLayout("insets 30, fill", "", "[]25[]"));

        var titleLabel = new JLabel("Sign in to App");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, "align center, span, wrap");

        usernameField = new PlaceholderTextField("Username");
        passwordField = new PlaceholderPasswordField("Password");

        add(usernameField, "align center, h 35!, w 300!, span");
        add(passwordField, "align center, h 35!, w 300!, span");

        successMessageLabel = new JLabel();
        successMessageLabel.setForeground(
                new Color(0, 128, 0)); // Example: Green color for success message
        successMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        successMessageLabel.setVisible(false); // Initially invisible
        add(successMessageLabel, "wrap, span");

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

    private void animateSuccessMessage(String message) {
        successMessageLabel.setText(message);
        successMessageLabel.setVisible(true);
        var alpha = new float[] {0f}; // Start with fully transparent

        var timer =
                new Timer(
                        50,
                        e -> {
                            alpha[0] += 0.05f; // Increase opacity
                            if (alpha[0] > 1f) {
                                alpha[0] = 1f;
                                ((Timer) e.getSource()).stop(); // Stop the timer when fully opaque
                            }
                            successMessageLabel.setForeground(
                                    new Color(0, 128, 0, (int) (alpha[0] * 255)));
                        });
        timer.setRepeats(true);
        timer.start();
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
        signUp(username, password, new Date());
    }

    private void signUp(String username, char[] password, Date dateOfBirth) {
        try {
            logger.info("Creating and saving Diffie-Hellman key pair");
            var keyPair = generateECDHKeyPair();
            var publicKeyString = exportPublicKey(keyPair.getPublic());
            storePrivateKey(keyPair, username, password);
            var registerReq =
                    new RegisterRequest(
                            username, new String(password), dateOfBirth, publicKeyString);
            var registerReqJson = objectMapper.writeValueAsString(registerReq);
            logger.info("Register request: {}", registerReqJson);

            var req =
                    HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/auth/register"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(registerReqJson))
                            .build();

            client.sendAsync(req, BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(this::parseRegisterResponse)
                    .thenAccept(this::handleRegisterResponse)
                    .exceptionally(
                            e -> {
                                logger.error("Error authenticating", e);
                                showErrorMessage("Register Failed: " + e.getMessage());
                                return null;
                            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void authenticate(String username, char[] password) {
        try {
            var loginReq = new LoginRequest(username, new String(password));
            var loginReqJson = objectMapper.writeValueAsString(loginReq);
            logger.info("Login request: {}", loginReqJson);
            logger.info("Loading Diffie-Hellman private key");
            var privateKey = loadPrivateKey(username, password);
            parent.setPrivateKey(privateKey);

            var req =
                    HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/auth/login"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(loginReqJson))
                            .build();

            setUser(username);
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
                                clearUser();
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

                    connectToSocket();

                    onSuccessfulLogin.run();
                });
    }

    private void setUser(String username) {
        parent.setUsername(username);
    }

    private void clearUser() {
        parent.clearUsername();
    }

    private void connectToSocket() {
        var socket = new StompSessionManager(client);
        socket.connect();
        parent.setSocket(socket);
    }

    private RegisterResponse parseRegisterResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, RegisterResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRegisterResponse(RegisterResponse response) {
        SwingUtilities.invokeLater(
                () -> {
                    //                    spinner.showCompletedTick();
                    if (response == null) {
                        showErrorMessage("Failed to parse server response or communication error.");
                    } else {
                        if (!response.isSuccess()) {
                            showErrorMessage(response.getMessage());
                            return;
                        }
                        animateSuccessMessage(response.getMessage());
                    }
                });
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
