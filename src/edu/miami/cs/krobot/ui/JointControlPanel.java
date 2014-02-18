package edu.miami.cs.krobot.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import edu.miami.cs.krobot.model.KinematicChain;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.ModelPart;
import edu.miami.cs.krobot.model.joint.Joint1DOF;
import edu.miami.cs.krobot.view.RobotView;

public class JointControlPanel extends JPanel {

  public JointControlPanel(final Model model, final RobotView robotView) {
    setLayout(new BorderLayout(0, 0));

    JPanel mainPanel = new JPanel();
    GridBagLayout gbl_panel = new GridBagLayout();
    gbl_panel.columnWidths = new int[] { 0, 0 };
    gbl_panel.rowHeights = new int[] { 0, 0 };
    gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
    mainPanel.setLayout(gbl_panel);
    
    JScrollPane scrollPane = new JScrollPane(mainPanel);
    add(scrollPane, BorderLayout.CENTER);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;

    for (KinematicChain chain : model.getChains()) {
      JPanel chainPanel = new JPanel();
      chainPanel.setLayout(new GridLayout(0, 1));
      chainPanel.setBorder(new TitledBorder(chain.name));

      int added = 0;
      for (ModelPart part : chain.parts) {
        if (part instanceof Joint1DOF) {
          Joint1DOFControl control = new Joint1DOFControl(model, (Joint1DOF) part, robotView);
          chainPanel.add(control);
          added++;
        }
      }

      if (added > 0) {
        mainPanel.add(chainPanel, gbc);
        gbc.gridy++;
      }
    }

    JButton printButton = new JButton("Print Status");
    printButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        System.out.println(model.getStatus());
      }
    });
    mainPanel.add(printButton, gbc);

    gbc.gridy++;
    JButton resetButton = new JButton("Zero Joints");
    resetButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        for (ModelPart body : model.getBodies()) {
          if (body instanceof Joint1DOF) {
            Joint1DOF joint = (Joint1DOF) body;
            joint.setValue(0f);
            model.commitUpdate();
            robotView.repaint();
          }
        }
      }
    });
    mainPanel.add(resetButton, gbc);
  }
}
