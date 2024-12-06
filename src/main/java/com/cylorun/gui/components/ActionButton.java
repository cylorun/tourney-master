package com.cylorun.gui.components;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ActionButton extends JButton {
    public ActionButton(String label, ActionListener al) {
        super(label);
        this.addActionListener(al);
    }
}
