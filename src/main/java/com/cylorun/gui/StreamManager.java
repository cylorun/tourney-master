package com.cylorun.gui;

import com.cylorun.model.Player;
import com.cylorun.obs.OBSController;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StreamManager extends JPanel {

    private final List<StreamerPanel> selectedStreamers;
    private final List<Player> ttvNames;

    public StreamManager(List<Player> ttvNames, int rows, int cols) {
        super(new GridLayout(rows, cols, 10, 10));

        this.ttvNames = ttvNames;
        this.selectedStreamers = new ArrayList<>();
        Player prev = new Player("place", "holder");

        for (int i = 0; i < rows * cols; i++) {
            Player streamer = ttvNames.size() > i ? ttvNames.get(i) : prev;
            prev = streamer;

            StreamerPanel streamerPanel = new StreamerPanel(streamer, this.ttvNames, i + 1);
            streamerPanel.onCheckBoxChange(() -> {
                this.selectedStreamers.add(streamerPanel);

                if (this.selectedStreamers.size() == 2) {
                    this.swapPanels(this.selectedStreamers.get(0), this.selectedStreamers.get(1));
                    this.selectedStreamers.forEach(s -> s.setCheckBoxSelected(false));
                    this.selectedStreamers.clear();
                }

            });
            this.add(streamerPanel);
        }
    }

    /**
     * Swap properties of two StreamerPanel instances.
     */
    public void swapPanels(StreamerPanel a, StreamerPanel b) {
        System.out.println("Swapping: " + a.streamer + ", " + b.streamer);

        Player tempStreamer = a.streamer;
        a.setStreamer(b.streamer);
        b.setStreamer(tempStreamer);

        a.updateDropdownSelection();
        b.updateDropdownSelection();

        OBSController.getInstance().swapPlayerSources(a.getIdx(), b.getIdx());
    }

    public static class StreamerPanel extends JPanel {
        private final JCheckBox activeCheck;
        private Player streamer;
        private final JComboBox<String> streamerDropdown;
        private Runnable onCheckBoxChange;
        private final int idx; // 1 based index for obs

        public StreamerPanel(Player streamer, List<Player> ttvNames, int idx) {
            super(new BorderLayout());
            this.streamer = streamer;
            this.idx = idx;

            this.streamerDropdown = new JComboBox<>(ttvNames.stream().map(p -> p.getTwitch()).toArray(String[]::new));
            this.streamerDropdown.setSelectedItem(streamer.getTwitch());

            this.add(streamerDropdown, BorderLayout.CENTER);

            this.streamerDropdown.addActionListener((e -> {
                String selectedTwitch = (String) this.streamerDropdown.getSelectedItem();
                this.streamer = ttvNames.stream()
                        .filter(p -> p.getTwitch().equals(selectedTwitch))
                        .findFirst()
                        .orElse(this.streamer);

                OBSController.getInstance().editPlayerSource(this.idx, this.streamer.getTwitch(), this.streamer.getLabel());
            }));

            this.activeCheck = new JCheckBox();
            this.activeCheck.addActionListener((e) -> {
                if (this.onCheckBoxChange != null) {
                    this.onCheckBoxChange.run();
                }
            });

            this.add(activeCheck, BorderLayout.EAST);
        }

        public void setCheckBoxSelected(boolean b) {
            this.activeCheck.setSelected(b);
        }

        public void setStreamer(Player p) {
            this.streamer = p;
            this.streamerDropdown.setSelectedItem(p.getTwitch());
        }

        public int getIdx() {
            return this.idx;
        }

        public void updateDropdownSelection() {
            this.streamerDropdown.setSelectedItem(this.streamer.getTwitch());
        }

        public void onCheckBoxChange(Runnable onChange) {
            this.onCheckBoxChange = onChange;
        }
    }
}
