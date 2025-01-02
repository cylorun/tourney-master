package com.cylorun.gui;

import com.cylorun.TourneyMaster;
import com.cylorun.TourneyMasterOptions;
import com.cylorun.paceman.Paceman;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EventPaceWindow extends JFrame {

    private JPanel pacePanel;
    private static EventPaceWindow instance;

    private EventPaceWindow() {
        super("Event Pace");
        this.initUI();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() ->
                SwingUtilities.invokeLater(this::reloadRuns), 0L, 15L, TimeUnit.SECONDS);
    }

    private void initUI() {
        this.setLayout(new BorderLayout());

        JLabel headerLabel = new JLabel("Pace Tracker", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        this.add(headerLabel, BorderLayout.NORTH);

        this.pacePanel = new JPanel();
        this.pacePanel.setLayout(new BoxLayout(this.pacePanel, BoxLayout.Y_AXIS));
        this.pacePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(new JScrollPane(this.pacePanel), BorderLayout.CENTER);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(400, 500);
        this.setLocationRelativeTo(null);
    }

    private void reloadRuns() {
        this.pacePanel.removeAll();

        String eventId = TourneyMasterOptions.getInstance().paceman_eventid;
        boolean hasData = false;

        for (JsonObject r : Paceman.getPaceForEvent(eventId)) {
            Paceman.Pace pace = Paceman.getLastEventPace(r);
            if (pace == null) continue;

            hasData = true;

            JLabel runnerLabel = new JLabel(String.format("%s, %s at %s", pace.runner, pace.split, TourneyMaster.msToString(pace.lastTime)));
            runnerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            runnerLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            this.pacePanel.add(runnerLabel);
        }

        if (!hasData) {
            JLabel noDataLabel = new JLabel("No pace data available.", JLabel.CENTER);
            noDataLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            noDataLabel.setForeground(Color.GRAY);
            this.pacePanel.add(noDataLabel);
        }

        this.pacePanel.repaint();
        this.pacePanel.revalidate();
    }

    public void open() {
        this.setVisible(true);
    }

    public static EventPaceWindow getInstance() {
        if (instance == null) {
            instance = new EventPaceWindow();
        }
        return instance;
    }
}
