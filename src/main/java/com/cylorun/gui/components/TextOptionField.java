package com.cylorun.gui.components;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.function.Consumer;

public class TextOptionField extends JPanel {
    private JComponent textField;

    private boolean numbersOnly = false;

    public TextOptionField(String label, String value, Consumer<String> consumer) {
        this(label, value, false, consumer);
    }

    public TextOptionField(String label, String value, boolean isPasswordField, Consumer<String> consumer) {
        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.textField = isPasswordField ? createPasswordField(value) : createTextField(value);
        this.add(new JLabel(label));
        this.add(this.textField);

        if (numbersOnly && this.textField instanceof JTextField) {
            applyNumberFilter((JTextField) this.textField);
        }

        this.addChangeListener(this.textField, consumer);
    }

    public void setValue(String newValue) {
        ((JTextField) this.textField).setText(newValue);
    }

    public String getValue() {
        return ((JTextField) this.textField).getText();
    }

    public TextOptionField numbersOnly() {
        this.numbersOnly = true;
        if (this.textField instanceof JTextField) {
            applyNumberFilter((JTextField) this.textField);
        }
        return this;
    }

    private JComponent createTextField(String value) {
        JTextField textField = new JTextField(value);
        textField.setPreferredSize(new Dimension(200, 25));
        return textField;
    }

    private JComponent createPasswordField(String value) {
        JPasswordField passwordField = new JPasswordField(value);
        passwordField.setPreferredSize(new Dimension(200, 25));
        return passwordField;
    }

    private void addChangeListener(JComponent component, Consumer<String> consumer) {
        if (component instanceof JTextField textField) {
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    consumer.accept(textField.getText());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    consumer.accept(textField.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    consumer.accept(textField.getText());
                }
            });
        } else if (component instanceof JPasswordField passwordField) {
            passwordField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    consumer.accept(String.valueOf(passwordField.getPassword()));
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    consumer.accept(String.valueOf(passwordField.getPassword()));
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    consumer.accept(String.valueOf(passwordField.getPassword()));
                }
            });
        }
    }

    private void applyNumberFilter(JTextField textField) {
        AbstractDocument doc = (AbstractDocument) textField.getDocument();
        doc.setDocumentFilter(new NumericFilter());
    }

    private static class NumericFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (isNumeric(string)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
            if (isNumeric(string)) {
                super.replace(fb, offset, length, string, attrs);
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length);
        }

        private boolean isNumeric(String str) {
            return str != null && str.matches("\\d*"); // Allows only digits
        }
    }
}
