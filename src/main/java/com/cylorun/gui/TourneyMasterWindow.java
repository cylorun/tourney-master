package com.cylorun.gui;

import com.cylorun.TourneyMasterOptions;
import com.cylorun.gui.components.BooleanOptionField;
import com.cylorun.gui.components.TextOptionField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TourneyMasterWindow extends JFrame {
    private static TourneyMasterWindow instance;

    private JPanel commentatorView;
    private JPanel hostView;
    private JPanel currentView;

    private JPanel streamersPanel;

    private TourneyMasterWindow() {
        this.setTitle("Tourney Master " + com.cylorun.TourneyMaster.VERSION);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 400);
        this.setLayout(new BorderLayout());

        this.initComponents();

        this.setVisible(true);
    }

    private void initComponents() {
        this.commentatorView = createCommentatorView();
        this.hostView = createHostView();

        // commentator view as default
        String lastView = TourneyMasterOptions.getInstance().lastView;
        this.currentView = lastView.equals("host") ? this.hostView : this.commentatorView;

        this.add(this.currentView, BorderLayout.CENTER);
    }

    private JPanel createCommentatorView() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel webhookPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        webhookPanel.add(new JLabel("Webhook URL: "));
        JTextField webhookField = new JTextField(30);
        webhookPanel.add(webhookField);
        panel.add(webhookPanel, BorderLayout.NORTH);

        panel.add(createStreamersPanel(), BorderLayout.CENTER);

        panel.add(createSwitchViewButton("Switch to Host View"), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHostView() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel hostSettingsPanel = new JPanel();
        hostSettingsPanel.setLayout(new GridLayout(4, 2, 5, 5));

        TextOptionField passwordField = new TextOptionField("WebSocket Password: ", "", true, (v) -> {});
        hostSettingsPanel.add(passwordField);

        TextOptionField portField = new TextOptionField("WebSocket Port:", "", (v) -> {});
        hostSettingsPanel.add(portField);

        BooleanOptionField enableCommentatorsCheck = new BooleanOptionField("Enable Commentators", true, (v) -> {});
        hostSettingsPanel.add(enableCommentatorsCheck);

        hostSettingsPanel.add(new JLabel());

        panel.add(hostSettingsPanel, BorderLayout.NORTH);

        panel.add(createStreamersPanel(), BorderLayout.CENTER);

        panel.add(createSwitchViewButton("Switch to Commentator View"), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStreamersPanel() {
        this.streamersPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        for (int i = 0; i < 9; i++) {
            JPanel streamerPanel = new JPanel(new BorderLayout());

            JComboBox<String> streamerDropdown = new JComboBox<>(TourneyMasterOptions.getInstance().streamers.toArray(new String[0]));

            streamerPanel.add(streamerDropdown, BorderLayout.CENTER);

            JCheckBox activeCheck = new JCheckBox();
            streamerPanel.add(activeCheck, BorderLayout.EAST);

            this.streamersPanel.add(streamerPanel);
        }
        return this.streamersPanel;
    }

    private JPanel createSwitchViewButton(String buttonText) {
        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton switchButton = new JButton(buttonText);
        switchButton.addActionListener(this::switchView);
        switchPanel.add(switchButton);
        return switchPanel;
    }

    private void switchView(ActionEvent event) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to switch views?",
                "Confirm Switch",
                JOptionPane.YES_NO_OPTION);

        TourneyMasterOptions options = TourneyMasterOptions.getInstance();
        if (confirm == JOptionPane.YES_OPTION) {
            this.remove(this.currentView);
            if (this.currentView == this.commentatorView) {
                this.currentView = this.hostView;
                options.lastView = "host";
                TourneyMasterOptions.save();
            } else {
                this.currentView = this.commentatorView;
                options.lastView = "commentator";
                TourneyMasterOptions.save();
            }
            this.add(this.currentView, BorderLayout.CENTER);
            this.revalidate();
            this.repaint();
        }
    }

    public static TourneyMasterWindow getInstance() {
        if (instance == null) {
            instance = new TourneyMasterWindow();
        }

        return instance;
    }
}
