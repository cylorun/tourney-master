package com.cylorun.gui;

import com.cylorun.model.Player;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class StreamManager extends JPanel {

    private List<StreamerPanel> selectedStreamers;
    private List<Player> ttvNames;
    public StreamManager(List<Player> ttvNames, int rows, int cols) {
        super(new GridLayout(rows, cols, 10, 10));

        this.ttvNames = ttvNames;
        this.selectedStreamers = new ArrayList<>();
        Player prev = new Player("place", "holder");

        for (int i = 0; i < rows * cols; i++) {
            Player streamer = ttvNames.size() > i ?  ttvNames.get(i) : prev;
            prev = streamer;
            StreamerPanel streamerPanel = new StreamerPanel(streamer, this.ttvNames);
            streamerPanel.onCheckBoxChange(() -> {
                System.out.println("Click: " + streamerPanel.ttvName);
                this.selectedStreamers.add(streamerPanel);

                if (this.selectedStreamers.size() == 2) {
                    this.swapPanels(this.selectedStreamers.get(0), this.selectedStreamers.get(1));
                    this.selectedStreamers.clear();
                }

            });
            this.add(streamerPanel);
        }
    }

    public void swapPanels(StreamerPanel a, StreamerPanel b) {
        System.out.println("Swapping: " + a.ttvName + ", " + b.ttvName);
    }

    public static class StreamerPanel extends JPanel {
        private Player ttvName;
        private Runnable onCheckBoxChange;

        public StreamerPanel(Player streamer, List<Player> ttvNames) {
            super(new BorderLayout());
            this.ttvName = streamer;

            JComboBox<String> streamerDropdown = new JComboBox<>(ttvNames.stream().map(p -> p.twitch).toArray(String[]::new));

            streamerDropdown.setSelectedItem(streamer);
            this.add(streamerDropdown, BorderLayout.CENTER);

            JCheckBox activeCheck = new JCheckBox();
            activeCheck.addActionListener((e) -> {
                if (this.onCheckBoxChange != null) {
                    this.onCheckBoxChange.run();
                }
            });

            this.add(activeCheck, BorderLayout.EAST);
        }

        public void onCheckBoxChange(Runnable onChange) {
            this.onCheckBoxChange = onChange;
        }

    }
}
