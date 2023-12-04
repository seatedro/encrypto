/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.encrypto.EncryptoClient;

import static org.slf4j.LoggerFactory.getLogger;

import com.encrypto.EncryptoClient.components.ChatPanel;
import com.encrypto.EncryptoClient.components.LoginSignupPanel;
import com.encrypto.EncryptoClient.dto.UserDTO;
import com.encrypto.EncryptoClient.util.StompSessionManager;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;

import lombok.Getter;
import lombok.Setter;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.security.PrivateKey;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.*;

public class EncryptoClient {
    private static final Logger logger = getLogger(EncryptoClient.class);
    private JFrame frame;
    private LoginSignupPanel loginSignupPanel;
    private ChatPanel chatPanel;
    @Getter @Setter private PrivateKey privateKey;
    @Getter @Setter private StompSessionManager socket;

    @Getter
    private static final ConcurrentHashMap<String, UserDTO> chats = new ConcurrentHashMap<>();

    @Getter @Setter private static String username;

    public static final HttpClient client =
            HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                    .build();

    public EncryptoClient() {}

    private void render() {
        FlatMacDarkLaf.setup();
        frame = new JFrame("Encrypto");
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new MigLayout());

        if (SystemInfo.isMacFullWindowContentSupported) {
            frame.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
            frame.getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
            frame.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
            frame.getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);
        }

        // Login/Signup
        loginSignupPanel = new LoginSignupPanel(this);
        frame.add(loginSignupPanel, "push, grow");
        //        chatPanel = new ChatPanel();
        //        frame.add(chatPanel, "push, grow");

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void transitionToChatPanel() {
        if (chatPanel == null) {
            chatPanel = new ChatPanel(getSocket(), client);
        }

        frame.remove(loginSignupPanel);
        frame.add(chatPanel, "push, grow");
        frame.validate();
        frame.repaint();
    }

    public void clearUsername() {
        username = null;
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        logger.info("Starting Encrypto Client");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.appearance", "system");
        SwingUtilities.invokeLater(() -> new EncryptoClient().render());
    }
}
