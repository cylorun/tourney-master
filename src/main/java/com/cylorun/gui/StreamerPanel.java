package com.cylorun.gui;

import com.cylorun.TourneyMasterOptions;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StreamerPanel extends JPanel {

    public StreamerPanel(List<String> ttvNames, int rows, int cols) {
        super(new GridLayout(rows, cols, 10, 10));

        for (int i = 0; i < rows * cols; i++) {
            JPanel singlePanel = new JPanel(new BorderLayout());

            JComboBox<String> streamerDropdown = new JComboBox<>(ttvNames.toArray(new String[0]));

            singlePanel.add(streamerDropdown, BorderLayout.CENTER);

            JCheckBox activeCheck = new JCheckBox();
            singlePanel.add(activeCheck, BorderLayout.EAST);

            this.add(singlePanel);
        }
    }
}
