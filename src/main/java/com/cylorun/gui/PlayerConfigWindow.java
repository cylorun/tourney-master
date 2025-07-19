package com.cylorun.gui;

import com.cylorun.TourneyMasterOptions;
import com.cylorun.gui.components.ActionButton;
import com.cylorun.model.Player;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PlayerConfigWindow extends JFrame {

    private JTable playerTable;
    private DefaultTableModel tableModel;
    private boolean isOpen = false;

    private static PlayerConfigWindow instance;

    private PlayerConfigWindow() {
        super("Player Config");
        this.initUI();
    }

    public static synchronized PlayerConfigWindow getInstance() {
        if (PlayerConfigWindow.instance == null) {
            PlayerConfigWindow.instance = new PlayerConfigWindow();
        }

        return PlayerConfigWindow.instance;
    }

    private void initUI() {
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setSize(600, 400);
        this.setLayout(new BorderLayout());

        this.tableModel = new DefaultTableModel(new Object[]{"Twitch Name", "Label (name/pronouns/whatever)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 1;
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if (value instanceof String str) {
                    System.out.println(str);
//                    PlayerConfigWindow.this.save();
                }

                super.setValueAt(value, row, col);
            }
        };

        this.playerTable = new JTable(this.tableModel);
        this.playerTable.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(this.playerTable);

        this.playerTable.getDefaultEditor(String.class).addCellEditorListener(new javax.swing.event.CellEditorListener() {
            @Override
            public void editingStopped(javax.swing.event.ChangeEvent e) {
                PlayerConfigWindow.this.save();
            }

            @Override
            public void editingCanceled(javax.swing.event.ChangeEvent e) {
            }
        });


        this.playerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int row = PlayerConfigWindow.this.playerTable.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        PlayerConfigWindow.this.playerTable.setRowSelectionInterval(row, row);
                        PlayerConfigWindow.this.showPopup(e, row);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = PlayerConfigWindow.this.playerTable.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        PlayerConfigWindow.this.playerTable.setRowSelectionInterval(row, row);
                        PlayerConfigWindow.this.showPopup(e, row);
                    }
                }
            }
        });

        JButton addButton = new JButton("+");
        addButton.setFont(new Font("Arial", Font.BOLD, 20));
        addButton.addActionListener(e -> PlayerConfigWindow.this.addNewPlayer());


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(new ActionButton("Remove all", (e) -> {
            if (JOptionPane.showConfirmDialog(this, "are you sure") == JOptionPane.YES_OPTION) {
                for (int i = this.tableModel.getRowCount() - 1; i >= 0; i--) {
                    this.tableModel.removeRow(i);
                }

                this.save();
            }
        }));

        this.add(scrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        this.loadData();
    }

    public void open() {
        this.setVisible(true);
    }

    private void save() {
        TableModel model = playerTable.getModel();
        TourneyMasterOptions options = TourneyMasterOptions.getInstance();

        options.players.clear();
        for (int row = 0; row < model.getRowCount(); row++) {
            String twitchName = (String) model.getValueAt(row, 0); // col 1
            String ign = (String) model.getValueAt(row, 1); // col 2

            System.out.println("Row " + row + ": Twitch Name = " + twitchName + ", Player Label= " + ign);
            options.players.add(new Player(twitchName, ign));
        }

        TourneyMasterOptions.save();
    }

    private void addNewPlayer() {
        this.tableModel.addRow(new Object[]{"New Player", "they/them 7:01 pb"});
    }

    private void loadData() {
        TourneyMasterOptions options  = TourneyMasterOptions.getInstance();

        for (Player p : options.players) {
            this.tableModel.addRow(new Object[]{p.getTwitch(), p.getLabel()});
        }
    }

    private void showPopup(MouseEvent e, int row) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Edit");
        editItem.addActionListener(ev -> {
            PlayerConfigWindow.this.playerTable.editCellAt(row, 0);
        });

        JMenuItem removeItem = new JMenuItem("Remove");
        removeItem.addActionListener(ev -> {
            PlayerConfigWindow.this.tableModel.removeRow(row);
        });

        popupMenu.add(editItem);
        popupMenu.add(removeItem);

        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
}