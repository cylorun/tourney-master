package com.cylorun.gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class NumberOptionField extends JPanel {
    public NumberOptionField(String label, String toolTipText, Integer value, Consumer<Integer> consumer) {
        this.setLayout(new FlowLayout(FlowLayout.CENTER));

        SpinnerModel spinnerModel = new SpinnerNumberModel(Integer.parseInt(String.valueOf(value)), 0, Integer.MAX_VALUE, 1); // what the actual fuck did i have to do here
        JSpinner inputField = new JSpinner(spinnerModel);
        inputField.setPreferredSize(new Dimension(70, 25));
        JLabel l = new JLabel(label);
        l.setToolTipText(toolTipText);
        this.add(l);
        this.add(inputField);
        inputField.addChangeListener(e -> consumer.accept((Integer) inputField.getValue()));
    }
}