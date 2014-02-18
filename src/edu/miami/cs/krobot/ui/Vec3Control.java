package edu.miami.cs.krobot.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.miami.cs.js.math.vector.Vec3;

public class Vec3Control extends JPanel {

  interface Listener {
    void changed(Vec3 v);
  }

  JSpinner            xSpinner;
  JSpinner            ySpinner;
  JSpinner            zSpinner;
  final Vec3          v;
  ArrayList<Listener> listeners = new ArrayList<Listener>();

  public Vec3Control(Vec3 initial, double min, double max, double step) {

    this.v = initial;

    Vec3ChangeListener vecChangeListener = new Vec3ChangeListener();

    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[] { 0, 80, 0, 80, 0, 80, 0 };
    gridBagLayout.rowHeights = new int[] { 0, 0 };
    gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
    gridBagLayout.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
    setLayout(gridBagLayout);

    JLabel lblX = new JLabel("X");
    GridBagConstraints gbc_lblX = new GridBagConstraints();
    gbc_lblX.insets = new Insets(0, 0, 0, 5);
    gbc_lblX.gridx = 0;
    gbc_lblX.gridy = 0;
    add(lblX, gbc_lblX);

    xSpinner = new JSpinner(new SpinnerNumberModel(0.0, min, max, step));
    xSpinner.setValue(((Float)initial.x).doubleValue());
    xSpinner.addChangeListener(vecChangeListener);
    GridBagConstraints gbc_xSpinner = new GridBagConstraints();
    gbc_xSpinner.fill = GridBagConstraints.HORIZONTAL;
    gbc_xSpinner.insets = new Insets(0, 0, 0, 5);
    gbc_xSpinner.gridx = 1;
    gbc_xSpinner.gridy = 0;
    add(xSpinner, gbc_xSpinner);

    JLabel lblY = new JLabel("Y");
    GridBagConstraints gbc_lblY = new GridBagConstraints();
    gbc_lblY.insets = new Insets(0, 0, 0, 5);
    gbc_lblY.gridx = 2;
    gbc_lblY.gridy = 0;
    add(lblY, gbc_lblY);

    ySpinner = new JSpinner(new SpinnerNumberModel(0.0, min, max, step));
    ySpinner.setValue(((Float)initial.y).doubleValue());
    ySpinner.addChangeListener(vecChangeListener);
    GridBagConstraints gbc_ySpinner = new GridBagConstraints();
    gbc_ySpinner.fill = GridBagConstraints.HORIZONTAL;
    gbc_ySpinner.insets = new Insets(0, 0, 0, 5);
    gbc_ySpinner.gridx = 3;
    gbc_ySpinner.gridy = 0;
    add(ySpinner, gbc_ySpinner);

    JLabel lblZ = new JLabel("Z");
    GridBagConstraints gbc_lblZ = new GridBagConstraints();
    gbc_lblZ.insets = new Insets(0, 0, 0, 5);
    gbc_lblZ.gridx = 4;
    gbc_lblZ.gridy = 0;
    add(lblZ, gbc_lblZ);

    zSpinner = new JSpinner(new SpinnerNumberModel(0.0, min, max, step));
    zSpinner.setValue(((Float)initial.z).doubleValue());
    zSpinner.addChangeListener(vecChangeListener);
    GridBagConstraints gbc_zSpinner = new GridBagConstraints();
    gbc_zSpinner.fill = GridBagConstraints.HORIZONTAL;
    gbc_zSpinner.gridx = 5;
    gbc_zSpinner.gridy = 0;
    add(zSpinner, gbc_zSpinner);
  }

  private class Vec3ChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      v.x = ((Double)xSpinner.getValue()).floatValue();
      v.y = ((Double)ySpinner.getValue()).floatValue();
      v.z = ((Double)zSpinner.getValue()).floatValue();
      for (Listener l : listeners) {
        l.changed(v);
      }
    }
  }
}
