package edu.miami.cs.krobot.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.joint.Joint1DOF;
import edu.miami.cs.krobot.view.RobotView;

public class Joint1DOFControl extends JPanel implements Joint1DOF.Listener {

  final JSpinner spinner;
  final JSlider  slider;

  public Joint1DOFControl(final Model model, final Joint1DOF joint, final RobotView robotView) {
    joint.addListener(this);

    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[] { 150, 85, 0, 0 };
    gridBagLayout.rowHeights = new int[] { 0, 0 };
    gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
    gridBagLayout.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
    setLayout(gridBagLayout);

    JLabel lblJointname = new JLabel(joint.getName());
    lblJointname.setHorizontalAlignment(SwingConstants.RIGHT);
    GridBagConstraints gbc_lblJointname = new GridBagConstraints();
    gbc_lblJointname.anchor = GridBagConstraints.EAST;
    gbc_lblJointname.insets = new Insets(0, 0, 0, 5);
    gbc_lblJointname.gridx = 0;
    gbc_lblJointname.gridy = 0;
    add(lblJointname, gbc_lblJointname);

    SpinnerModel spinModel = new SpinnerNumberModel(joint.getValue(), Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY, 1);
    spinner = new JSpinner(spinModel);
    spinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (!joint.isChangingValue()) {
          joint.setValue(((Double) spinner.getValue()).floatValue());
          robotView.repaint();
        }
      }
    });
    GridBagConstraints gbc_spinner = new GridBagConstraints();
    gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
    gbc_spinner.insets = new Insets(0, 0, 0, 5);
    gbc_spinner.gridx = 1;
    gbc_spinner.gridy = 0;
    add(spinner, gbc_spinner);

    slider = new JSlider(0, 100);
    slider.setValue((int) (joint.getNormValue() * 100));
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (!joint.isChangingValue()) {
          joint.setNormValue(slider.getValue() / 100.0f);
          robotView.repaint();
          if (!slider.getValueIsAdjusting())
            model.commitUpdate();
        }
      }
    });
    GridBagConstraints gbc_slider = new GridBagConstraints();
    gbc_slider.fill = GridBagConstraints.HORIZONTAL;
    gbc_slider.gridx = 2;
    gbc_slider.gridy = 0;
    add(slider, gbc_slider);

    slider.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        robotView.setSelectedJoint(joint);
        robotView.repaint();
      }

      @Override
      public void mouseExited(MouseEvent arg0) {
        robotView.setSelectedJoint(null);
        robotView.repaint();
      }
    });
  }

  @Override
  public void valueChanged(Joint1DOF joint, float newValue) {
    spinner.setValue(new Double(joint.getValue()));
    slider.setValue((int) (joint.getNormValue() * 100));
  }
}
