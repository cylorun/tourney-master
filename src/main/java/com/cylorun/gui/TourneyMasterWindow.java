package com.cylorun.gui;

import com.cylorun.TourneyMaster;
import com.cylorun.TourneyMasterOptions;
import com.cylorun.gui.components.ActionButton;
import com.cylorun.gui.components.BooleanOptionField;
import com.cylorun.gui.components.NumberOptionField;
import com.cylorun.gui.components.TextOptionField;
import com.cylorun.paceman.PacemanLB;
import com.cylorun.obs.OBSController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

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

        scenePanel.add(new ActionButton("Intermission", (e)-> {
            OBSController.getInstance().openScene("Intermission");
        }));

        scenePanel.add(new ActionButton("Main", (e)-> {
            OBSController.getInstance().openScene("Main");
        }));

        return scenePanel;
    }


    private JPanel createCommentatorConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        return panel;
    }

    private JPanel getHostConfigPanel() {
        TourneyMasterOptions options = TourneyMasterOptions.getInstance();
        JPanel hostSettingsPanel = new JPanel(new GridBagLayout());

        hostSettingsPanel.setBorder(BorderFactory.createTitledBorder("General Settings"));

        BooleanOptionField enableCommentatorsCheck = new BooleanOptionField("Enable Commentators", options.enable_commentators, (newVal) -> {
            options.enable_commentators = newVal;
            TourneyMasterOptions.save();
        });

        BooleanOptionField enablePacemanLb = new BooleanOptionField("Enable Paceman LB", options.enable_paceman_lb, (newVal) -> {
            options.enable_paceman_lb = newVal;
            TourneyMasterOptions.save();
        });

        TextOptionField pacemanEventId = new TextOptionField("Paceman Event id", options.paceman_eventid, (newVal) -> {
           options.paceman_eventid = newVal;
           PacemanLB.getInstance().setEventId(newVal);
           TourneyMasterOptions.save();
        });

        NumberOptionField maxLbEntries = new NumberOptionField("Max LB Entries", options.max_lb_entries, (newVal) -> {
            options.max_lb_entries = newVal;
            TourneyMasterOptions.save();
        });

        ActionButton playerButton = new ActionButton("Players", (e) -> {
            PlayerConfigWindow.getInstance().open();
        });

        ActionButton paceButton = new ActionButton("Show Pace", (e) -> {
            EventPaceWindow.getInstance().open();
        });

        ActionButton openConfigFolder = new ActionButton("Open Config Folder", (e) -> {
            try {
                Desktop.getDesktop().open(TourneyMasterOptions.getTrackerDir().toFile());
            } catch (IOException ex) {
                TourneyMaster.log(Level.SEVERE, "Failed to open config folder ;?");
                throw new RuntimeException(ex);
            }
        });

        ActionButton genPlayerSources = new ActionButton("Create Player Sources", (e) -> {
            this.createPlayerSources();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        hostSettingsPanel.add(enablePacemanLb, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        hostSettingsPanel.add(enableCommentatorsCheck, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        hostSettingsPanel.add(pacemanEventId, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        hostSettingsPanel.add(maxLbEntries, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        hostSettingsPanel.add(playerButton, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        hostSettingsPanel.add(genPlayerSources, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        hostSettingsPanel.add(paceButton, gbc);

        gbc.gridx = 1;
//        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        hostSettingsPanel.add(openConfigFolder, gbc);


        return hostSettingsPanel;
    }

    private void createPlayerSources() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to create player sources?\nThis will mess up the previous sources", "Confirmation", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {
            String ans = JOptionPane.showInputDialog(this, "Generate sources for how many players", 6);
            try {
                int count = Integer.parseInt(ans);
                OBSController.getInstance().genPlayerSources("Main", count);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Value must be a whole number", "Invalid input", JOptionPane.WARNING_MESSAGE);
            }
        }
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
