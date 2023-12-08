package com.encrypto.EncryptoClient.elements;

import net.miginfocom.swing.MigLayout;

import java.awt.*;

import javax.swing.*;

public class MessageBubble extends JPanel {
    private static final int MAX_BUBBLE_WIDTH = 300; // Maximum bubble width
    private static final int ARC_WIDTH = 15; // Rounded corner arc width
    private static final int ARC_HEIGHT = 15; // Rounded corner arc height
    private static final int HORIZONTAL_PADDING = 10; // Horizontal padding within the bubble
    private static final int VERTICAL_PADDING = 10; // Vertical padding within the bubble

    private JLabel messageLabel;
    private boolean isReceiver;

    public MessageBubble(String message, boolean isReceiver) {
        setLayout(new MigLayout("insets 5", "[grow, fill]", "[]"));
        setOpaque(false);
        this.isReceiver = isReceiver;

        // Create a label for the message text
        messageLabel =
                new JLabel(
                        "<html><body style='width: "
                                + (MAX_BUBBLE_WIDTH - 10 * HORIZONTAL_PADDING)
                                + "px'>"
                                + message
                                + "</body></html>");
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setBorder(
                BorderFactory.createEmptyBorder(
                        VERTICAL_PADDING,
                        HORIZONTAL_PADDING,
                        VERTICAL_PADDING,
                        HORIZONTAL_PADDING));
        add(messageLabel);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Determine the size of the text to set the bubble size
        var labelSize = messageLabel.getPreferredSize();
        var width = Math.min(labelSize.width + 2 * HORIZONTAL_PADDING, MAX_BUBBLE_WIDTH);
        var height = labelSize.height + 2 * VERTICAL_PADDING;

        // Set bubble color
        g2.setColor(isReceiver ? new Color(4, 118, 212) : Color.DARK_GRAY);
        // Draw rounded rectangle bubble
        g2.fillRoundRect(0, 0, width, height, ARC_WIDTH, ARC_HEIGHT);
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        // Ensure that the bubble's preferred size takes the label's preferred size into account
        var labelSize = messageLabel.getPreferredSize();
        var width = Math.min(labelSize.width + 2 * HORIZONTAL_PADDING, MAX_BUBBLE_WIDTH);
        var height = labelSize.height + 2 * VERTICAL_PADDING;
        return new Dimension(width, height);
    }
}
