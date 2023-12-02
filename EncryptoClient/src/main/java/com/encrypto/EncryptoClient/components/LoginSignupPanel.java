package com.encrypto.EncryptoClient.components;

import com.encrypto.EncryptoClient.elements.PlaceholderPasswordField;
import com.encrypto.EncryptoClient.elements.PlaceholderTextField;
import com.encrypto.EncryptoClient.util.ValidationUtils;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Predicate;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class LoginSignupPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(LoginSignupPanel.class);
    private static JTextField usernameField;
    private static JPasswordField passwordField;

    private static JButton loginButton;
    private static JButton signupButton;
    private static JLabel usernameErrorLabel;
    private static JLabel passwordErrorLabel;

    private final Border defaultBorder = new JTextField().getBorder();
    private final Border errorBorder = BorderFactory.createLineBorder(Color.RED, 1);

    public LoginSignupPanel() {
        setLayout(new MigLayout("insets 30, fill", "", "[]25[]"));

        var titleLabel = new JLabel("Sign in to App");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, "align center, span, wrap");

        usernameField = new PlaceholderTextField("Username");
        passwordField = new PlaceholderPasswordField("Password");

        usernameErrorLabel = new JLabel();
        passwordErrorLabel = new JLabel();

        usernameErrorLabel.setForeground(Color.RED);
        passwordErrorLabel.setForeground(Color.RED);

        add(usernameField, "align center, h 35!, w 300!, span");
        add(usernameErrorLabel, "wrap");

        add(passwordField, "align center, h 35!, w 300!, span");
        add(passwordErrorLabel, "wrap");

        usernameField.getDocument().putProperty("owner", usernameField);
        passwordField.getDocument().putProperty("owner", passwordField);
        passwordField.getDocument().putProperty("isPassword", true);

        setupValidation(
                usernameField.getDocument(), usernameErrorLabel, ValidationUtils::isValidEmail);
        setupValidation(
                passwordField.getDocument(),
                passwordErrorLabel,
                ValidationUtils::isValidComplexPassword);

        loginButton = new JButton("Login");
        signupButton = new JButton("Signup");

        add(loginButton, "align center, h 35!, w 100!, span");
        add(signupButton, "align center, h 35!, w 100!, span");

        loginButton.addActionListener(LoginSignupPanel::login);
        signupButton.addActionListener(LoginSignupPanel::signup);

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

    private void setupValidation(
            Document doc, JLabel errorLabel, Predicate<String> validationPredicate) {
        doc.addDocumentListener(
                new DocumentListener() {
                    void validate() {
                        var text = getText(doc);
                        var valid = validationPredicate.test(text);
                        String errorMessage = null;
                        // Check if it's the password field and validate accordingly
                        if (doc.getProperty("isPassword") != null) {
                            errorMessage =
                                    ValidationUtils.getPasswordValidationErrors(text.toCharArray());
                        } else {
                            errorMessage = "Invalid email format";
                        }
                        if (!valid) {
                            errorLabel.setText(errorMessage);
                            setBorderWithFocus(doc, errorBorder);
                        } else {
                            errorLabel.setText("");
                            setBorderWithFocus(doc, defaultBorder);
                        }
                    }

                    private void setBorderWithFocus(Document doc, Border border) {
                        SwingUtilities.invokeLater(
                                () -> {
                                    if (doc.getProperty("owner") instanceof JComponent owner) {
                                        var marginBorder =
                                                BorderFactory.createEmptyBorder(2, 5, 2, 2);
                                        var compoundBorder =
                                                BorderFactory.createCompoundBorder(
                                                        border, marginBorder);
                                        owner.setBorder(compoundBorder);
                                    }
                                });
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        validate();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        validate();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        validate();
                    }

                    private String getText(Document doc) {
                        try {
                            return doc.getText(0, doc.getLength());
                        } catch (BadLocationException e) {
                            return "";
                        }
                    }
                });
    }
}
