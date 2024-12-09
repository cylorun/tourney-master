package com.cylorun.gui;

import com.cylorun.TourneyMasterOptions;
import com.cylorun.gui.components.ActionButton;
import com.cylorun.gui.components.BooleanOptionField;
import com.cylorun.gui.components.TextOptionField;
import com.cylorun.obs.OBSController;
import io.obswebsocket.community.client.model.Scene;

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
        this.setSize(800, 600);
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

        panel.add(this.createStreamersPanel(), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(createSwitchScenePanel(), BorderLayout.CENTER);
        southPanel.add(createSwitchViewButton("Switch to Host View"), BorderLayout.SOUTH);

        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createHostView() {
        TourneyMasterOptions options = TourneyMasterOptions.getInstance();
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Add spacing
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        // Host Settings Panel
        JPanel hostSettingsPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // Flexible rows
        hostSettingsPanel.setBorder(BorderFactory.createTitledBorder("Connection Settings"));

        TextOptionField hostField = new TextOptionField("WebSocket Hostname: ", options.obs_host, (newVal) -> {
            options.obs_host = newVal;
            TourneyMasterOptions.save();
        });
        hostSettingsPanel.add(hostField);

        TextOptionField portField = new TextOptionField("WebSocket Port: ", String.valueOf(options.obs_port), (newVal) -> {
            options.obs_port = Integer.parseInt(newVal);
            TourneyMasterOptions.save();
        }).numbersOnly();
        hostSettingsPanel.add(portField);

        TextOptionField passwordField = new TextOptionField("WebSocket Password: ", options.obs_password, true, (newVal) -> {
            options.obs_password = newVal;
            TourneyMasterOptions.save();
        });
        hostSettingsPanel.add(passwordField);

        BooleanOptionField enableCommentatorsCheck = new BooleanOptionField("Enable Commentators", options.enable_commentators, (newVal) -> {
            options.enable_commentators = newVal;
            TourneyMasterOptions.save();
        });
        hostSettingsPanel.add(enableCommentatorsCheck);

        ActionButton connectButton = new ActionButton("Reconnect", (e) -> {
            try {
                OBSController.getInstance().connect(options.obs_host, options.obs_port, options.obs_password);
            } catch (Exception err) {
                err.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to connect to OBS WebSocket server: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        connectButton.setPreferredSize(new Dimension(120, 30)); // Set consistent button size
        hostSettingsPanel.add(connectButton);

        panel.add(hostSettingsPanel, BorderLayout.NORTH);

        // Streamers Panel
        JPanel streamersPanel = createStreamersPanel();
        streamersPanel.setBorder(BorderFactory.createTitledBorder("Stream Management"));
        panel.add(streamersPanel, BorderLayout.CENTER);

        // South Panel
        JPanel southPanel = new JPanel(new BorderLayout(5, 5)); // Add spacing
        JPanel switchScenePanel = createSwitchScenePanel();
        switchScenePanel.setBorder(BorderFactory.createTitledBorder("Scene Control"));
        southPanel.add(switchScenePanel, BorderLayout.CENTER);

        JPanel switchViewButton = createSwitchViewButton("Switch to Commentator View");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Align to the right
        buttonPanel.add(switchViewButton);

        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(southPanel, BorderLayout.SOUTH);

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

    private JPanel createSwitchScenePanel() {
        JPanel scenePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));


        JComboBox<String> sceneBox = new JComboBox<>();
        OBSController.getInstance().getSceneList((res) -> {
            if (res == null) {
                System.err.println("Failed to fetch scenes: response is null.");
                return;
            }
            SwingUtilities.invokeLater(() -> {
                for (Scene scene : res.getScenes()) {
                    sceneBox.addItem(scene.getSceneName());
                }
                sceneBox.revalidate();
                scenePanel.repaint();
            });
        });


        sceneBox.addActionListener((e) -> {
            OBSController.getInstance().openScene((String) sceneBox.getSelectedItem());
        });

        scenePanel.add(sceneBox);

        return scenePanel;
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
