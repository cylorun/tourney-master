package com.cylorun.gui;

import com.cylorun.TourneyMaster;
import com.cylorun.TourneyMasterOptions;
import com.cylorun.gui.components.*;
import com.cylorun.model.Pace;
import com.cylorun.model.PacemanEvent;
import com.cylorun.paceman.Paceman;
import com.cylorun.paceman.PacemanLB;
import com.cylorun.obs.OBSController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;

public class TourneyMasterWindow extends JFrame {
    private static TourneyMasterWindow instance;

    private JPanel commentatorView;
    private JPanel hostView;
    private JPanel currentView;
    private JPanel streamersPanel;

    private TourneyMasterWindow() {
        this.setTitle("Tourney Master " + com.cylorun.TourneyMaster.VERSION);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 650);
        this.setLayout(new BorderLayout());

        this.initComponents();

        this.setVisible(true);
    }

    private void initComponents() {
        this.commentatorView = this.createMainView("commentator");
        this.hostView = this.createMainView("host");

        String lastView = TourneyMasterOptions.getInstance().lastView;
        this.currentView = lastView.equals("host") ? this.hostView : this.commentatorView;

        this.add(this.currentView, BorderLayout.CENTER);
    }

    private void reloadStreamersPanel() {
        if (this.streamersPanel != null) {
            this.currentView.remove(this.streamersPanel);
        }

        this.streamersPanel = this.createStreamersPanel();
        this.streamersPanel.setBorder(BorderFactory.createTitledBorder("Stream Management"));
        this.currentView.add(this.streamersPanel, BorderLayout.CENTER);

        this.currentView.revalidate();
        this.currentView.repaint();
    }


    private JPanel createMainView(String role) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (role.equals("host")) {
            panel.add(this.getHostConfigPanel(), BorderLayout.NORTH);
        } else if (role.equals("commentator")) {
            panel.add(this.createCommentatorConfigPanel(), BorderLayout.NORTH);
        }

        this.streamersPanel = this.createStreamersPanel();
        this.streamersPanel.setBorder(BorderFactory.createTitledBorder("Stream Management"));
        panel.add(this.streamersPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        JPanel switchScenePanel = this.createSwitchScenePanel();
        switchScenePanel.setBorder(BorderFactory.createTitledBorder("Scene Control"));
        southPanel.add(switchScenePanel, BorderLayout.CENTER);

        JPanel switchViewButton = this.createSwitchViewButton(String.format("Switch to %s View", role.equals("host") ? "commentator" : "host"));
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

        scenePanel.add(new ActionButton("Intermission", (e) -> {
            OBSController.getInstance().openScene("Intermission");
        }));

        scenePanel.add(new ActionButton("Main", (e) -> {
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
//
//        BooleanOptionField enableCommentatorsCheck = new BooleanOptionField("Enable Commentators (WIP does nothing)", options.enable_commentators, (newVal) -> {
//            options.enable_commentators = newVal;
//            TourneyMasterOptions.save();
//        });

        BooleanOptionField enablePacemanLb = new BooleanOptionField("Enable Paceman LB", options.enable_paceman_lb, (newVal) -> {
            options.enable_paceman_lb = newVal;
            TourneyMasterOptions.save();
        });

        MultiChoiceOptionField pacemanEventId = new MultiChoiceOptionField(new String[]{}, options.paceman_eventid, "Paceman Event ID", (newVal) -> {
            Optional<PacemanEvent> opt = Paceman.getEventByVanity(newVal);
            if (opt.isPresent()) { // there is alr logging if it's empty in getEventByVanity
                options.paceman_eventid = opt.get()._id;
                options.paceman_eventvanity = opt.get().vanity;
                PacemanLB.getInstance().setEventId(newVal);
                TourneyMasterOptions.save();

                EventPaceWindow.getInstance().enabled = true; // set to true again, incase it got disabled before
            }
        });


        CompletableFuture.runAsync(() -> {
           String[] events = Paceman.getAllPacemanEvents()
                   .stream().map((e) -> e.vanity).toArray(String[]::new);
           pacemanEventId.setOptions(events, options.paceman_eventvanity);
        });

        NumberOptionField maxLbEntries = new NumberOptionField("Max LB Entries", options.max_lb_entries, (newVal) -> {
            options.max_lb_entries = newVal;
            TourneyMasterOptions.save();
        });

        NumberOptionField rowsOptionField = new NumberOptionField("Rows", options.rows, (newVal) -> {
            options.rows = newVal;

            this.reloadStreamersPanel();
            TourneyMasterOptions.save();
        });

        NumberOptionField colsOptionField = new NumberOptionField("Columns", options.cols, (newVal) -> {
            options.cols = newVal;

            this.reloadStreamersPanel();
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
                TourneyMaster.log(Level.SEVERE, "Failed to open config folder");
                throw new RuntimeException(ex);
            }
        });

        ActionButton genPlayerSources = new ActionButton("Create Player Sources", (e) -> {
            this.createPlayerSources();
        });

        // Layout Configurations
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10); // Added more spacing for better readability
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // first col
        gbc.gridx = 0;
        gbc.gridy = row++;
        hostSettingsPanel.add(enablePacemanLb, gbc);

//        gbc.gridy = row++;
//        hostSettingsPanel.add(enableCommentatorsCheck, gbc);

        gbc.gridy = row++;
        hostSettingsPanel.add(pacemanEventId, gbc);

        gbc.gridy = row++;
        hostSettingsPanel.add(maxLbEntries, gbc);

        gbc.gridy = row++;
        hostSettingsPanel.add(rowsOptionField, gbc);

        gbc.gridy = row++;
        hostSettingsPanel.add(colsOptionField, gbc);

        // 2nd col
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        hostSettingsPanel.add(playerButton, gbc);

        gbc.gridy++;
        hostSettingsPanel.add(genPlayerSources, gbc);

        gbc.gridy++;
        hostSettingsPanel.add(paceButton, gbc);

        gbc.gridy++;
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
        JOptionPane.showMessageDialog(this, "This is a WIP :0");
        if (true) {
            return;
        }
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
