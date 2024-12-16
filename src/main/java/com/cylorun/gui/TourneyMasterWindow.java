package com.cylorun.gui;

import com.cylorun.TourneyMaster;
import com.cylorun.TourneyMasterOptions;
import com.cylorun.gui.components.ActionButton;
import com.cylorun.gui.components.BooleanOptionField;
import com.cylorun.gui.components.TextOptionField;
import com.cylorun.obs.OBSController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TourneyMasterWindow extends JFrame {
    private static TourneyMasterWindow instance;

    private JPanel commentatorView;
    private JPanel hostView;
    private JPanel currentView;

    private TourneyMasterWindow() {
        this.setTitle("Tourney Master " + com.cylorun.TourneyMaster.VERSION);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 650);
        this.setLayout(new BorderLayout());

        this.initComponents();

        this.setVisible(true);

//        OBSController.getInstance(); // connects to obs
    }

    private void initComponents() {
        this.commentatorView = this.createMainView("commentator");
        this.hostView = this.createMainView("host");

        String lastView = TourneyMasterOptions.getInstance().lastView;
        this.currentView = lastView.equals("host") ? this.hostView : this.commentatorView;

        this.add(this.currentView, BorderLayout.CENTER);
    }


    private JPanel createMainView(String role) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (role.equals("host")) {
            panel.add(this.getHostConfigPanel(), BorderLayout.NORTH);
        } else if (role.equals("commentator")) {
            panel.add(this.createCommentatorConfigPanel(), BorderLayout.NORTH);
        }

        JPanel streamersPanel = createStreamersPanel();
        streamersPanel.setBorder(BorderFactory.createTitledBorder("Stream Management"));
        panel.add(streamersPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        JPanel switchScenePanel = createSwitchScenePanel();
        switchScenePanel.setBorder(BorderFactory.createTitledBorder("Scene Control"));
        southPanel.add(switchScenePanel, BorderLayout.CENTER);

        JPanel switchViewButton = createSwitchViewButton(String.format("Switch to %s View", role.equals("host") ? "commentator" : "host"));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(switchViewButton);

        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    private StreamManager createStreamersPanel() {
        TourneyMasterOptions options = TourneyMasterOptions.getInstance();
        return new StreamManager(options.players, options.rows, options.cols);
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


        OBSController.getInstance().getAllSceneNames((res) -> {
            if (res == null) {
                TourneyMaster.showError("Failed to fetch scenes: response is null.");
                return;
            }

            SwingUtilities.invokeLater(() -> {
                sceneBox.removeAllItems();
                for (String name : res) {
                    sceneBox.addItem(name);
                }
            });
        });

        sceneBox.addActionListener((e) -> {
            String item = (String) sceneBox.getSelectedItem();
            if (item == null) {
                return;
            }
            OBSController.getInstance().openScene(item);
        });

        scenePanel.add(sceneBox);

        return scenePanel;
    }


    private JPanel createCommentatorConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        return panel;
    }

    private JPanel getHostConfigPanel() {
        TourneyMasterOptions options = TourneyMasterOptions.getInstance();
        JPanel hostSettingsPanel = new JPanel(new GridBagLayout());

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


        ActionButton playerButton = new ActionButton("Players", (e) -> {
            PlayerConfigWindow.getInstance().open();
            ;
        });


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // spacing

        gbc.gridx = 0;
        gbc.gridy = 0;
        hostSettingsPanel.add(hostField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        hostSettingsPanel.add(portField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        hostSettingsPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        hostSettingsPanel.add(enableCommentatorsCheck, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        hostSettingsPanel.add(playerButton, gbc);

        return hostSettingsPanel;
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

    public synchronized static TourneyMasterWindow getInstance() {
        if (instance == null) {
            instance = new TourneyMasterWindow();
        }

        return instance;
    }
}
