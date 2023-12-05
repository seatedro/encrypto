package com.encrypto.EncryptoClient.elements;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;

public class PlaceHolderTextArea extends JTextArea implements FocusListener {

    private final String placeholder;
    private boolean showingPlaceholder;

    public PlaceHolderTextArea(String placeholder) {
        super(placeholder);
        this.placeholder = placeholder;
        this.showingPlaceholder = true;
        super.addFocusListener(this);
        updateStyle();
    }

    private void updateStyle() {
        if (showingPlaceholder) {
            setForeground(Color.GRAY);
        } else {
            setForeground(Color.WHITE);
            setText("");
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (this.getText().isEmpty()) {
            showingPlaceholder = false;
            updateStyle();
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.getText().isEmpty()) {
            showingPlaceholder = true;
            setText(placeholder);
            updateStyle();
        }
    }

    @Override
    public String getText() {
        return showingPlaceholder ? "" : super.getText();
    }
}
