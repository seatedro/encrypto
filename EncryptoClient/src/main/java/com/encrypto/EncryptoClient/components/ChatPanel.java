package com.encrypto.EncryptoClient.components;

import static java.lang.String.format;

import com.encrypto.EncryptoClient.EncryptoClient;
import com.encrypto.EncryptoClient.dto.MessageDTO;
import com.encrypto.EncryptoClient.dto.UserDTO;
import com.encrypto.EncryptoClient.dto.UserWithMessagesDTO;
import com.encrypto.EncryptoClient.dto.response.GetAllChatsResponse;
import com.encrypto.EncryptoClient.elements.MessageBubble;
import com.encrypto.EncryptoClient.elements.PlaceHolderTextArea;
import com.encrypto.EncryptoClient.service.ChatService;
import com.encrypto.EncryptoClient.service.UserService;
import com.encrypto.EncryptoClient.util.KeyUtils;
import com.encrypto.EncryptoClient.util.StompSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChatPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ChatPanel.class);
    private JList<String> chatList;
    private JPanel chatDisplayArea;
    private PlaceHolderTextArea messageInputField;
    private JButton sendMessageButton;
    private JPanel inputPanel;
    private JButton newChatButton;
    private DefaultListModel<String> chatListModel;
    @Getter private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatService chatService;
    private final UserService userService;
    @Getter @Setter private StompSessionManager socket;
    private static JPanel chatDisplayComponent;

    public ChatPanel(StompSessionManager socket, HttpClient client) {
        setSocket(socket);
        chatService = new ChatService(client);
        userService = new UserService(client);
        setLayout(new MigLayout("fill, insets 0", "[grow][grow]", "[grow][shrink 0]"));
        setPreferredSize(new Dimension(1200, 800));
        // Add some gap at the top of the panel.
        setBorder(new EmptyBorder(25, 0, 0, 0));

        populateChats(chatService.fetchAllChats(EncryptoClient.getUsername()));
        startListening();
        render();
    }

    private void render() {
        setupSideBar();
        setupChatDisplayArea();
        setupMessageInputArea();
    }

    private void setupSideBar() {
        chatList = new JList<>();
        chatListModel = new DefaultListModel<>();
        chatList.setModel(chatListModel);
        chatList.setCellRenderer(new ChatListCellRenderer());
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.addListSelectionListener(
                e -> {
                    if (!e.getValueIsAdjusting()) {
                        var selectedUser = chatList.getSelectedValue();
                        updateChatDisplayArea(selectedUser);
                    }
                });

        for (var username : EncryptoClient.getChats().keySet()) {
            chatListModel.addElement(username);
        }

        var chatScrollPane = new JScrollPane(chatList);
        var sideBarPanel = new JPanel(new MigLayout("fill, insets 0"));
        chatScrollPane.setBorder(null);
        setupNewChatButton();
        sideBarPanel.add(chatScrollPane, "grow, pushy");
        sideBarPanel.add(newChatButton, "dock north");
        add(sideBarPanel, "dock west, spany 2, width 200::200, growy");
    }

    private void addNewChat(String username) {
        if (!chatListModel.contains(username)) {
            if (handshake(username)) {
                chatListModel.addElement(username);
                chatList.setSelectedValue(username, true);
                updateChatDisplayArea(username);
            }
        }
    }

    private void updateChatDisplayArea(String username) {
        chatSelected();
        chatDisplayArea.removeAll();
        if (username != null) {
            chatDisplayArea.add(createChatDisplayComponentForUser(username), "grow, push");
            logger.info("Chat display area updated for user: {}", username);
        } else {
            chatDisplayArea.add(
                    new JLabel("Select a chat to start messaging", SwingConstants.CENTER),
                    "grow, push");
        }
        chatDisplayArea.revalidate();
        chatDisplayArea.repaint();
    }

    private void setupChatDisplayArea() {
        chatDisplayArea = new JPanel(new MigLayout("fill, insets 0"));

        var placeHolderLabel = new JLabel("Select a chat to begin", SwingConstants.CENTER);
        placeHolderLabel.setForeground(Color.GRAY);

        chatDisplayArea.add(placeHolderLabel, "push, align center");
        add(chatDisplayArea, "grow, push, span");
    }

    private void setupMessageInputArea() {
        messageInputField = new PlaceHolderTextArea("Type a message...");
        sendMessageButton = new JButton(new ImageIcon("assets/send.png"));
        sendMessageButton.setBorder(BorderFactory.createEmptyBorder());
        sendMessageButton.setContentAreaFilled(false);
        inputPanel = new JPanel(new MigLayout("insets 0 25 0 0"));
        messageInputField.setLineWrap(true);
        messageInputField.setWrapStyleWord(true);

        sendMessageButton.addActionListener(
                e -> {
                    var message = messageInputField.getText();
                    if (!message.isEmpty()) {
                        sendMessage(message);
                        messageInputField.setText("");
                    }
                });
        messageInputField.addKeyListener(
                new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            // Enter pressed
                            e.consume(); // Prevent newline.
                            var message = messageInputField.getText();
                            if (!message.isEmpty()) {
                                sendMessage(message);
                                messageInputField.setText("");
                            }
                        }
                    }
                });

        var messageInputScrollPane = new JScrollPane(messageInputField);
        messageInputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messageInputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        inputPanel.add(messageInputScrollPane, "pushx, growx");
        inputPanel.add(sendMessageButton, "width 80!, height 30!");
        inputPanel.setVisible(false);
        add(inputPanel, "dock south");
    }

    private void setupNewChatButton() {
        newChatButton =
                new JButton(new ImageIcon("assets/plus.png")); // Replace with your icon path
        newChatButton.setBorder(BorderFactory.createEmptyBorder());
        newChatButton.setContentAreaFilled(false);
        newChatButton.setFocusPainted(false);
        newChatButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Rounded.
        newChatButton.setBorderPainted(false);
        newChatButton.setOpaque(false);
        newChatButton.setPreferredSize(new Dimension(60, 60));
        newChatButton.addActionListener(e -> showNewChatDialog());
    }

    private void showNewChatDialog() {
        var username = JOptionPane.showInputDialog(this, "Enter username to chat with:");
        if (username != null && !username.trim().isEmpty()) {
            addNewChat(username);
        }
    }

    private JScrollPane createChatDisplayComponentForUser(String username) {
        // Create a panel to hold the chat messages
        chatDisplayComponent = new JPanel(new MigLayout("fillx, insets 0, wrap 1"));
        chatDisplayComponent.setBorder(new EmptyBorder(0, 10, 10, 10));

        var messages = EncryptoClient.getChats().get(username).getMessages();
        // Add a sample label, or you can add the actual chat messages here
        for (var msgObject : messages) {
            var msg = msgObject.getContent();
            var received = msgObject.getSenderId().equals(username);
            addMessageToChat(msg, received);
            // Align left for received messages, align right for sent messages
        }
        // Add more UI components to chatDisplayComponent as needed for the chat UI

        return new JScrollPane(chatDisplayComponent);
    }

    private static void addMessageToChat(String msg, boolean received) {
        var bubble = new MessageBubble(msg);
        // Align left for received messages, align right for sent messages
        var alignment = received ? "left" : "right";

        chatDisplayComponent.add(
                bubble, format("wrap, wmin 10, top, gapbottom 10, align %s", alignment));
        chatDisplayComponent.revalidate();
        chatDisplayComponent.repaint();
    }

    private static class ChatListCellRenderer implements ListCellRenderer<String> {
        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list,
                String username,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            var chatCellPanel = new JPanel(new MigLayout("fill, insets 10"));
            if (isSelected) {
                chatCellPanel.setBackground(Color.DARK_GRAY);
            }
            var usernameLabel = new JLabel(username);
            chatCellPanel.add(usernameLabel);
            return chatCellPanel;
        }
    }

    public void chatSelected() {
        // Check if the inputPanel is already added
        if (inputPanel.getParent() == null) {
            // Add the inputPanel to the ChatPanel layout
            this.add(inputPanel, "dock south, spanx, growx, h 50!");
        } else {
            // Make the inputPanel visible if it was hidden
            inputPanel.setVisible(true);
        }

        // Refresh the layout to show the inputPanel
        this.revalidate();
        this.repaint();
    }

    public void chatDeselected() {
        // Hide the inputPanel
        inputPanel.setVisible(false);
    }

    public void populateChats(GetAllChatsResponse response) {
        logger.info("Populating chats: {}", Arrays.toString(response.getUsernames()));
        for (var username : response.getUsernames()) {
            if (handshake(username)) {
                addNewChat(username);
            }
        }
    }

    private boolean handshake(String username) {
        var chat = EncryptoClient.getChats().get(username);
        if (chat == null || chat.getUser() == null || chat.getUser().getPublicKey().isEmpty()) {
            return fetchPublicKey(username);
        }
        return false;
    }

    private boolean fetchPublicKey(String username) {
        var publicKeyResponse = userService.getPublicKey(username);
        if (publicKeyResponse == null) {
            logger.error("Failed to fetch public key for {}", username);
            return false;
        }
        var publicKey = publicKeyResponse.getPublicKey();
        logger.info("Fetched public key for {}: {}", username, publicKey);
        if (!EncryptoClient.getChats().containsKey(username)) {
            EncryptoClient.getChats()
                    .put(
                            username,
                            new UserWithMessagesDTO(
                                    new UserDTO(username, ""), null, new ArrayList<>()));
        }
        EncryptoClient.getChats().get(username).getUser().setPublicKey(publicKey);
        return true;
    }

    private void startListening() {
        socket.subscribe(
                format("/user/%s/private", EncryptoClient.getUsername()),
                new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return MessageDTO.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        var message = (MessageDTO) payload;
                        logger.info("Received message: {}", message);
                        addMessageToUser(message);
                    }
                });
    }

    private void addMessageToUser(MessageDTO message) {
        var senderId = message.getSenderId();
        var receiverId = message.getReceiverId();
        var content = message.getContent();
        var timestamp = message.getTimestamp();
        var chat = EncryptoClient.getChats().get(senderId);
        if (chat == null) {
            logger.error("Chat not found for {}", senderId);
            return;
        }
        var messages = chat.getMessages();
        messages.add(new MessageDTO(senderId, receiverId, content, timestamp));
        if (chatList.getSelectedValue().equals(senderId)) {
            addMessageToChat(content, true);
        }
    }

    private void sendMessage(String message) {
        try {
            var selectedUser = chatList.getSelectedValue();
            var chat = EncryptoClient.getChats().get(selectedUser);
            var user = chat.getUser();
            var publicKey = KeyUtils.importPublicKey(user.getPublicKey());
            var privateKey = EncryptoClient.getPrivateKey();
            SecretKey secretKey;
            if (chat.getSecretKey() != null) {
                secretKey = chat.getSecretKey();
            } else {
                secretKey = KeyUtils.deriveSharedSecret(privateKey, publicKey);
                chat.setSecretKey(secretKey);
            }
            var encryptedMessage = KeyUtils.encryptMessage(message, secretKey);
            var encryptedMessageString = Base64.getEncoder().encodeToString(encryptedMessage);
            logger.info("Sending message to {}: {}", selectedUser, message);
            var socket = getSocket();
            socket.sendMessage(
                    EncryptoClient.getUsername(),
                    selectedUser,
                    encryptedMessageString,
                    Instant.now());
        } catch (Exception e) {
            logger.error("Error sending message", e);
        }
    }
}
