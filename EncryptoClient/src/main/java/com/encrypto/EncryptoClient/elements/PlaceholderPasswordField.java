package com.encrypto.EncryptoClient.elements;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;

public class PlaceholderPasswordField extends JPasswordField implements FocusListener {

    private final String placeholder;
    private boolean showingPlaceholder;

    public PlaceholderPasswordField(String placeholder) {
        super(placeholder);
        this.placeholder = placeholder;
        this.showingPlaceholder = true;
        super.addFocusListener(this);
        setEchoChar((char) 0); // To display placeholder as plain text
        updateStyle();
    }

    private void updateStyle() {
        if (showingPlaceholder) {
            setForeground(Color.GRAY);
            setEchoChar((char) 0);
        } else {
            setForeground(Color.WHITE);
            setEchoChar('•'); // Restore echo character for password masking
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (showingPlaceholder) {
            showingPlaceholder = false;
            setText("");
            updateStyle();
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.getPassword().length == 0) {
            showingPlaceholder = true;
            setText(placeholder);
            updateStyle();
        }
    }

    @Override
    public char[] getPassword() {
        return showingPlaceholder ? new char[0] : super.getPassword();
    }
}
