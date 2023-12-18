package com.encrypto.EncryptoClient.components;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChatPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ChatPanel.class);
    private static int nextMessageRow = 10000;
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
    private static JScrollPane chatScrollPane;

    //    Border redBorder = BorderFactory.createLineBorder(Color.RED, 1);

    @Setter EncryptoClient encryptoClient;

    public ChatPanel(EncryptoClient frame, StompSessionManager socket, HttpClient client) {
        setSocket(socket);
        setEncryptoClient(frame);
        chatService = new ChatService(client);
        userService = new UserService(client);
        setLayout(new MigLayout("fill, insets 0", "[grow][grow]", "[grow][shrink 0]"));
        setPreferredSize(new Dimension(1200, 800));
        // Add some gap at the top of the panel.
        setBorder(new EmptyBorder(25, 0, 0, 0));

        startListening();
        render();
    }

    private void render() {
        setupSideBar();
        setupChatDisplayArea();
        setupMessageInputArea();
        populateChats(chatService.fetchAllChats(EncryptoClient.getUsername()));
        onFocusChange();
    }

    private void onFocusChange() {
        encryptoClient
                .getFrame()
                .addWindowFocusListener(
                        new WindowFocusListener() {
                            @Override
                            public void windowGainedFocus(WindowEvent e) {
                                socket.sendPresenceStatus(EncryptoClient.getUsername(), true);
                            }

                            @Override
                            public void windowLostFocus(WindowEvent e) {
                                socket.sendPresenceStatus(EncryptoClient.getUsername(), false);
                            }
                        });
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
            addUserToChatList(username);
        }

        var chatScrollPane = new JScrollPane(chatList);
        var sideBarPanel = new JPanel(new MigLayout("fill, insets 0"));
        chatScrollPane.setBorder(null);
        setupNewChatButton();
        sideBarPanel.add(chatScrollPane, "grow, pushy");
        sideBarPanel.add(newChatButton, "dock north");
        add(sideBarPanel, "dock west, spany 2, width 200::200, growy");
    }

    private void addUserToChatList(String username) {
        chatListModel.addElement(username);
    }

    private void addNewChat(String username) {
        if (!chatListModel.contains(username)) {
            if (handshake(username)) {
                addUserToChatList(username);
                chatList.setSelectedValue(username, true);
                updateChatDisplayArea(username);
            }
        }
    }

    private void updateChatDisplayArea(String username) {
        chatSelected();
        chatDisplayArea.removeAll();
        if (username != null) {
            createChatDisplayComponentForUser(username);
            chatDisplayArea.add(chatScrollPane, "grow, push");
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

    private void createChatDisplayComponentForUser(String username) {
        // Create a panel to hold the chat messages
        chatDisplayComponent =
                new JPanel(new MigLayout("fillx, insets 0, wrap 1", "grow", "push[]"));
        chatDisplayComponent.setBorder(new EmptyBorder(0, 10, 10, 10));
        chatDisplayComponent.addComponentListener(
                new ComponentAdapter() {
                    public void componentResized(ComponentEvent e) {
                        scrollToBottom();
                    }
                });

        var messages = EncryptoClient.getChats().get(username).getMessages();
        // Add a sample label, or you can add the actual chat messages here
        for (var msgObject : messages) {
            var msg = msgObject.getContent();
            var received = msgObject.getSenderId().equals(username);
            addMessageToChat(msg, received);
            // Align left for received messages, align right for sent messages
        }
        // Add more UI components to chatDisplayComponent as needed for the chat UI

        chatScrollPane = new JScrollPane(chatDisplayComponent);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    private static void addMessageToChat(String msg, boolean received) {
        var bubble = new MessageBubble(msg, received);
        // Align left for received messages, align right for sent messages
        var alignment = received ? "left" : "right";

        chatDisplayComponent.add(
                bubble,
                format("cell 0 %d, wrap, gapbottom 10, align %s", nextMessageRow, alignment));
        nextMessageRow++;
        chatDisplayComponent.revalidate();
        chatDisplayComponent.repaint();
        if (chatScrollPane != null) {
            scrollToBottom();
        }
    }

    private static void scrollToBottom() {
        var vertical = chatScrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
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
        logger.info("Populating chats: {}", response.getChats());
        var usernames = response.getChats().keySet();
        for (var username : usernames) {
            if (handshake(username)) {
                addNewChat(username);
                for (var message : response.getChats().get(username)) {
                    var received = message.getSenderId().equals(username);
                    var decryptedMessage = decryptMessage(message, received);
                    addMessageToUser(message, received);
                }
            }
        }
    }

    private boolean handshake(String username) {
        var chat = getChat(username);
        if (chat == null || chat.getUser() == null || chat.getUser().getPublicKey().isEmpty()) {
            return fetchPublicKey(username);
        }
        return true;
    }

    private boolean fetchPublicKey(String username) {
        var publicKeyResponse = userService.getPublicKey(username);
        if (publicKeyResponse == null) {
            logger.error("Failed to fetch public key for {}", username);
            return false;
        }
        var publicKey = publicKeyResponse.getPublicKey();
        if (!EncryptoClient.getChats().containsKey(username)) {
            EncryptoClient.getChats()
                    .put(
                            username,
                            new UserWithMessagesDTO(
                                    new UserDTO(username, ""), null, false, new ArrayList<>()));
        }
        EncryptoClient.getChats().get(username).getUser().setPublicKey(publicKey);
        return true;
    }

    private void startListening() {
        logger.info("User {} listening for messages", EncryptoClient.getUsername());
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
                        try {
                            var messageJson = objectMapper.writeValueAsString(message);
                            logger.info("Received message: {}", messageJson);
                        } catch (JsonProcessingException e) {
                            logger.error("Error parsing message", e);
                        }
                        // Decrypt message.
                        var senderId = message.getSenderId();
                        var chat = getChat(senderId);
                        if (chat == null) {
                            EncryptoClient.getChats()
                                    .put(
                                            senderId,
                                            new UserWithMessagesDTO(
                                                    new UserDTO(senderId, null),
                                                    null,
                                                    true,
                                                    new ArrayList<>()));
                            addUserToChatList(senderId);
                            logger.error("Chat not found for {}, creating a new chat.", senderId);
                        }
                        var decryptedMessage = decryptMessage(message, true);
                        logger.info("Decrypted message: {}", decryptedMessage);
                        addMessageToUser(message, true);
                    }
                });
        socket.subscribe(
                "/topic/presence",
                new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return String.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        var username = (String) payload;
                        logger.info("Received presence update for {}", username);
                        var chat = getChat(username);
                        if (chat == null) return;
                        var isOnline = headers.get("isOnline").getFirst().equals("true");
                        chat.setOnline(isOnline);
                        chatList.repaint();
                    }
                });
    }

    private static SecretKey getSecretKey(
            UserWithMessagesDTO chat, PrivateKey privateKey, PublicKey publicKey) {
        SecretKey secretKey;
        if (chat.getSecretKey() != null) {
            secretKey = chat.getSecretKey();
        } else {
            secretKey = KeyUtils.deriveSharedSecret(privateKey, publicKey);
            chat.setSecretKey(secretKey);
        }
        return secretKey;
    }

    private String decryptMessage(MessageDTO message, boolean received) {
        var userId = getUserId(message, received);
        var chat = getChat(userId);
        fetchPublicKey(userId);
        var user = getUserFromChat(chat);
        var publicKey = KeyUtils.importPublicKey(user.getPublicKey());
        var privateKey = EncryptoClient.getPrivateKey();
        var secretKey = getSecretKey(chat, privateKey, publicKey);
        var decryptedMessage = KeyUtils.decryptMessage(message.getContent(), secretKey);
        message.setContent(decryptedMessage);
        return decryptedMessage;
    }

    private static UserWithMessagesDTO getChat(String senderId) {
        return EncryptoClient.getChats().get(senderId);
    }

    private UserDTO getUserFromChat(UserWithMessagesDTO chat) {
        return chat.getUser();
    }

    private void addMessageToUser(MessageDTO message, boolean received) {
        var userId = getUserId(message, received);
        var content = message.getContent();
        var chat = getChat(userId);
        var messages = chat.getMessages();
        messages.add(message);
        if (chatList.getSelectedValue().equals(userId)) {
            addMessageToChat(content, received);
        }
    }

    private static String getUserId(MessageDTO message, boolean received) {
        String userId;
        if (received) {
            userId = message.getSenderId();
        } else {
            userId = message.getReceiverId();
        }
        return userId;
    }

    private void sendMessage(String message) {
        try {
            var selectedUser = chatList.getSelectedValue();
            var chat = getChat(selectedUser);
            var user = getUserFromChat(chat);
            var publicKey = KeyUtils.importPublicKey(user.getPublicKey());
            var secretKey = getSecretKey(chat, EncryptoClient.getPrivateKey(), publicKey);
            var encryptedMessage = KeyUtils.encryptMessage(message, secretKey);
            var encryptedMessageString = Base64.getEncoder().encodeToString(encryptedMessage);
            logger.info("Sending message to {}: {}", selectedUser, message);
            var socket = getSocket();
            socket.sendMessage(
                    EncryptoClient.getUsername(),
                    selectedUser,
                    encryptedMessageString,
                    Instant.now());
            addMessageToUser(
                    new MessageDTO(
                            EncryptoClient.getUsername(),
                            selectedUser,
                            message,
                            ISO_INSTANT.format(Instant.now())),
                    false);
            //            addMessageToChat(message, false);
        } catch (Exception e) {
            logger.error("Error sending message", e);
        }
    }
}
