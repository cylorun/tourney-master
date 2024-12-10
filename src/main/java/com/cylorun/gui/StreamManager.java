package com.cylorun.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StreamManager extends JPanel {

    private List<String> ttvNames;
    public StreamManager(List<String> ttvNames, int rows, int cols) {
        super(new GridLayout(rows, cols, 10, 10));

        this.ttvNames = ttvNames;

        String prevName = "";
        for (int i = 0; i < rows * cols; i++) {
            String streamer = ttvNames.size() > i ?  ttvNames.get(i) : prevName;
            prevName = streamer;
            StreamerPanel streamerPanel = new StreamerPanel(streamer,this.ttvNames);

            this.add(streamerPanel);
        }
    }


    public static class StreamerPanel extends JPanel {
        private String ttvName;
        public StreamerPanel(String ttvName, List<String> ttvNames) {
            super(new BorderLayout());
            this.ttvName = ttvName;

            JComboBox<String> streamerDropdown = new JComboBox<>(ttvNames.toArray(new String[0]));

            streamerDropdown.setSelectedItem(ttvName);
            this.add(streamerDropdown, BorderLayout.CENTER);

            JCheckBox activeCheck = new JCheckBox();
            activeCheck.addActionListener((e) -> {

            });

            this.add(activeCheck, BorderLayout.EAST);
        }

    }
}
