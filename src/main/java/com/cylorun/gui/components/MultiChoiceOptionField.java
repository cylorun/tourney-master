package com.cylorun.gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class MultiChoiceOptionField extends JPanel {
    private JComboBox<String> comboBox;

    public MultiChoiceOptionField(String[] options, String value, String label, Consumer<String> consumer) {
        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.comboBox = new JComboBox<>(options);
        SwingUtilities.invokeLater(() -> comboBox.setSelectedItem(value));
        comboBox.addActionListener(e -> consumer.accept((String) comboBox.getSelectedItem()));
        this.add(new JLabel(label));
        this.add(comboBox);
    }

    public void setOptions(String[] newOptions) {
        this.comboBox.removeAllItems();
        for (String option : newOptions) {
            this.comboBox.addItem(option);
        }
    }

    public String getValue() {
        return this.comboBox.getSelectedItem().toString();
    }
}
