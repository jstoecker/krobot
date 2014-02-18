package edu.miami.cs.krobot.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.ModelPart;
import edu.miami.cs.krobot.model.solid.PolygonMesh;
import edu.miami.cs.krobot.view.RobotView;

public class MeshControlPanel extends JPanel {
  
  public MeshControlPanel(Model model, RobotView robotView) {
    setLayout(new BorderLayout(0, 0));
    
    JPanel panel = new JPanel();
    GridBagLayout gbl_panel = new GridBagLayout();
    gbl_panel.columnWidths = new int[]{0, 0};
    gbl_panel.rowHeights = new int[]{0, 0};
    gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
    panel.setLayout(gbl_panel);
    
    JScrollPane scrollPane = new JScrollPane(panel);
    add(scrollPane, BorderLayout.CENTER);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    for (ModelPart body : model.getBodies()) {
      if (body instanceof PolygonMesh) {
        panel.add(new MeshBodyControl((PolygonMesh)body, robotView), gbc);
        gbc.gridy++;
      }
    }
    
    panel.add(new JPanel(), gbc);
  }
}
