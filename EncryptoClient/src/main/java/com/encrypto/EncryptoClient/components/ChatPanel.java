package com.encrypto.EncryptoClient.components;

import com.encrypto.EncryptoClient.elements.PlaceholderTextField;

import net.miginfocom.swing.MigLayout;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class ChatPanel extends JPanel {
    private JList<JPanel> chatList;
    private JPanel chatDisplayArea;
    private JTextField messageInputField;
    private JButton sendMessageButton;
    private JPanel inputPanel;
    private JButton newChatButton;

    public ChatPanel() {
        setLayout(new MigLayout("insets 30, fill", "[grow 0][grow]", "[grow][]"));
        setPreferredSize(new Dimension(1200, 800));
        // Add some gap at the top of the panel.
        setBorder(new EmptyBorder(25, 0, 0, 0));

        render();
    }

    private void render() {
        setupSideBar();
        setupChatDisplayArea();
        setupMessageInputArea();
        //        setupNewChatButton();
    }

    private void setupSideBar() {
        chatList = new JList<>();
        var chatListModel = new DefaultListModel<JPanel>();
        chatList.setModel(chatListModel);
        chatList.setCellRenderer(new ChatListCellRenderer());

        addChatEntry(chatListModel, "Test User 1");
        addChatEntry(chatListModel, "Test User 2");

        var chatScrollPane = new JScrollPane(chatList);
        chatScrollPane.setBorder(null);
        add(chatScrollPane, "dock west, spany 2, width 200::200, growy");
    }

    private void addChatEntry(DefaultListModel<JPanel> model, String username) {
        var chatButton = new JButton(username);
        chatButton.setPreferredSize(new Dimension(200, 50));
        chatButton.setHorizontalAlignment(SwingConstants.LEFT);
        chatButton.setBorder(new EmptyBorder(10, 10, 10, 10));
        chatButton.addActionListener(
                e -> {
                    // Logic to open chat goes here
                });

        var panel = new JPanel(new MigLayout("fill, insets 0"));
        panel.add(chatButton, "grow");
        panel.setBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY));
        model.addElement(panel);
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
        newChatButton.addActionListener(e -> showNewChatDialog());
        add(newChatButton, "dock south");
    }

    private void showNewChatDialog() {
        String username = JOptionPane.showInputDialog(this, "Enter username to chat with:");
        if (username != null && !username.trim().isEmpty()) {
            // Logic to handle the new chat creation goes here
        }
    }

    private static class ChatListCellRenderer implements ListCellRenderer<JPanel> {
        @Override
        public Component getListCellRendererComponent(
                JList<? extends JPanel> list,
                JPanel value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            return value;
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
}
