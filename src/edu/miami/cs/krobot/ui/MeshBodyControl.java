package edu.miami.cs.krobot.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.model.solid.PolygonMesh;
import edu.miami.cs.krobot.view.GLView;

public class MeshBodyControl extends JPanel {
  
  public MeshBodyControl(final PolygonMesh mesh, final GLView view) {
    setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
    
    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[] { 121, 53, 0, 0 };
    gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
    gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
    gridBagLayout.rowWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
    setLayout(gridBagLayout);

    JLabel lblJointname = new JLabel(mesh.getName());
    GridBagConstraints gbc_lblJointname = new GridBagConstraints();
    gbc_lblJointname.insets = new Insets(0, 0, 5, 5);
    gbc_lblJointname.gridx = 0;
    gbc_lblJointname.gridy = 0;
    add(lblJointname, gbc_lblJointname);
    
    JLabel lblTranslation = new JLabel("Trans.");
    GridBagConstraints gbc_lblTranslation = new GridBagConstraints();
    gbc_lblTranslation.insets = new Insets(0, 0, 5, 5);
    gbc_lblTranslation.gridx = 1;
    gbc_lblTranslation.gridy = 0;
    add(lblTranslation, gbc_lblTranslation);
    
    Vec3Control transPanel = new Vec3Control(mesh.getTranslation(), -1000.0, 1000.0, 0.1);
    transPanel.listeners.add(new Vec3Control.Listener() {
      public void changed(Vec3 v) {
        mesh.setTranslation(v);
        view.repaint();
      }
    });
    GridBagConstraints gbc_panel = new GridBagConstraints();
    gbc_panel.insets = new Insets(0, 0, 5, 0);
    gbc_panel.fill = GridBagConstraints.BOTH;
    gbc_panel.gridx = 2;
    gbc_panel.gridy = 0;
    add(transPanel, gbc_panel);
    
    JLabel lblRotation = new JLabel("Rot.");
    GridBagConstraints gbc_lblRotation = new GridBagConstraints();
    gbc_lblRotation.insets = new Insets(0, 0, 5, 5);
    gbc_lblRotation.gridx = 1;
    gbc_lblRotation.gridy = 1;
    add(lblRotation, gbc_lblRotation);
    
    Vec3Control rotPanel = new Vec3Control(mesh.getRotation(), -360, 360, 1);
    rotPanel.listeners.add(new Vec3Control.Listener() {
      public void changed(Vec3 v) {
        mesh.setRotation(v);
        view.repaint();
      }
    });
    GridBagConstraints gbc_panel_1 = new GridBagConstraints();
    gbc_panel_1.insets = new Insets(0, 0, 5, 0);
    gbc_panel_1.fill = GridBagConstraints.BOTH;
    gbc_panel_1.gridx = 2;
    gbc_panel_1.gridy = 1;
    add(rotPanel, gbc_panel_1);
    
    JLabel lblScale = new JLabel("Scale");
    GridBagConstraints gbc_lblScale = new GridBagConstraints();
    gbc_lblScale.insets = new Insets(0, 0, 0, 5);
    gbc_lblScale.gridx = 1;
    gbc_lblScale.gridy = 2;
    add(lblScale, gbc_lblScale);
    
    Vec3Control scalePanel = new Vec3Control(mesh.getScale(), -1000.0, 1000.0, 0.1);
    scalePanel.listeners.add(new Vec3Control.Listener() {
      public void changed(Vec3 v) {
        mesh.setScale(v);
        view.repaint();
      }
    });
    GridBagConstraints gbc_panel_2 = new GridBagConstraints();
    gbc_panel_2.fill = GridBagConstraints.BOTH;
    gbc_panel_2.gridx = 2;
    gbc_panel_2.gridy = 2;
    add(scalePanel, gbc_panel_2);
  }
}
