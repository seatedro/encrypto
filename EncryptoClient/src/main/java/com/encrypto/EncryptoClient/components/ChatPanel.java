package com.encrypto.EncryptoClient.components;

import com.encrypto.EncryptoClient.EncryptoClient;
import com.encrypto.EncryptoClient.dto.response.GetAllChatsResponse;
import com.encrypto.EncryptoClient.elements.MessageBubble;
import com.encrypto.EncryptoClient.elements.PlaceholderTextField;
import com.encrypto.EncryptoClient.service.ChatService;
import com.encrypto.EncryptoClient.service.UserService;
import com.encrypto.EncryptoClient.util.StompSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.http.HttpClient;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChatPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ChatPanel.class);
    private JList<String> chatList;
    private JPanel chatDisplayArea;
    private JTextField messageInputField;
    private JButton sendMessageButton;
    private JPanel inputPanel;
    private JButton newChatButton;
    private DefaultListModel<String> chatListModel;
    @Getter private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatService chatService;
    private final UserService userService;

    public ChatPanel(StompSessionManager socket, HttpClient client) {
        chatService = new ChatService(client);
        userService = new UserService(client);
        setLayout(new MigLayout("fill, insets 0", "[grow][grow]", "[grow][shrink 0]"));
        setPreferredSize(new Dimension(1200, 800));
        // Add some gap at the top of the panel.
        setBorder(new EmptyBorder(25, 0, 0, 0));

        populateChats(chatService.fetchAllChats(EncryptoClient.getUsername()));
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

        chatListModel.addElement("Test User 1");
        chatListModel.addElement("Test User 2");

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
            chatListModel.addElement(username);
            chatList.setSelectedValue(username, true);
            updateChatDisplayArea(username);
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
        messageInputField = new PlaceholderTextField("Type a message...");
        sendMessageButton = new JButton(new ImageIcon("assets/send.png"));
        sendMessageButton.setBorder(BorderFactory.createEmptyBorder());
        sendMessageButton.setContentAreaFilled(false);

        inputPanel = new JPanel(new MigLayout("insets 0 25 0 0"));
        inputPanel.add(messageInputField, "pushx, growx");
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
        var chatDisplayComponent = new JPanel(new MigLayout("fillx, insets 0, wrap 1"));
        chatDisplayComponent.setBorder(new EmptyBorder(0, 10, 10, 10));
        //        chatDisplayComponent.setBackground(Color.); // Dark theme background

        // Add a sample label, or you can add the actual chat messages here
        for (var i = 0; i < 15; i++) {
            var msg = "This is a sample message " + i;
            var bubble = new MessageBubble(msg);
            chatDisplayComponent.add(bubble, "wrap, wmin 10, top, gapbottom" + 10);
            chatDisplayComponent.revalidate();
            chatDisplayComponent.repaint();
            // Align left for received messages, align right for sent messages
        }
        // Add more UI components to chatDisplayComponent as needed for the chat UI

        return new JScrollPane(chatDisplayComponent);
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
            addNewChat(username);
            handshake(username);
        }
    }

    private void handshake(String username) {
        if (EncryptoClient.getChats().get(username).getPublicKey().isEmpty()) {
            fetchPublicKey(username);
        }
    }

    private void fetchPublicKey(String username) {
        var publicKey = userService.getPublicKey(username).getPublicKey();
        logger.info("Fetched public key for {}: {}", username, publicKey);
        EncryptoClient.getChats().get(username).setPublicKey(publicKey);
    }
}
