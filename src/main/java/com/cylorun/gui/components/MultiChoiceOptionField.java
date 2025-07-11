package com.cylorun.gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class MultiChoiceOptionField extends JPanel {
    private JComboBox<String> comboBox;
    private boolean suppressEvents = false;

    public MultiChoiceOptionField(String[] options, String value, String label, Consumer<String> consumer) {
        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.comboBox = new JComboBox<>(options);

        comboBox.addActionListener(e -> {
            if (!suppressEvents) {
                consumer.accept((String) comboBox.getSelectedItem());
            }
        });

        SwingUtilities.invokeLater(() -> comboBox.setSelectedItem(value));
        this.add(new JLabel(label));
        this.add(comboBox);
    }

    public void setOptions(String[] newOptions, String defaultVal) {
        suppressEvents = true;
        this.comboBox.removeAllItems();
        for (String option : newOptions) {
            this.comboBox.addItem(option);
        }

        this.comboBox.setSelectedItem(defaultVal);
        suppressEvents = false;
    }

    public String getValue() {
        return this.comboBox.getSelectedItem().toString();
    }
}
