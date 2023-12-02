package com.encrypto.EncryptoClient.components;

import com.encrypto.EncryptoClient.elements.PlaceholderPasswordField;
import com.encrypto.EncryptoClient.elements.PlaceholderTextField;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;

public class LoginSignupPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(LoginSignupPanel.class);
    private static JTextField usernameField;
    private static JPasswordField passwordField;

    private static JButton loginButton;
    private static JButton signupButton;

    public LoginSignupPanel() {
        setLayout(new MigLayout("insets 30, fill", "", "[]25[]"));

        var titleLabel = new JLabel("Sign in to App");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, "align center, span, wrap");

        usernameField = new PlaceholderTextField("Username");
        add(usernameField, "align center, h 35!, w 300!, span");

        passwordField = new PlaceholderPasswordField("Password");
        add(passwordField, "align center, h 35!, w 300!, span");

        loginButton = new JButton("Login");
        signupButton = new JButton("Signup");

        add(loginButton, "align center, h 35!, w 100!, span");
        add(signupButton, "align center, h 35!, w 100!, span");

        loginButton.addActionListener(LoginSignupPanel::login);
        signupButton.addActionListener(LoginSignupPanel::signup);
    }

    private static void login(ActionEvent e) {
        var username = usernameField.getText();
        var password = passwordField.getPassword();
        logger.info("Login attempt with username: {}", username);
    }

    private static void signup(ActionEvent e) {
        var username = usernameField.getText();
        var password = passwordField.getPassword();
        logger.info("Register attempt with username: {}", username);
    }
}
