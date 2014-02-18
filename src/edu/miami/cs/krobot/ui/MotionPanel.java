package edu.miami.cs.krobot.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.miami.cs.krobot.Config;
import edu.miami.cs.krobot.MainApp;
import edu.miami.cs.krobot.MainControl;
import edu.miami.cs.krobot.MotionPlayer;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.ModelPose;
import edu.miami.cs.krobot.model.PoseInterpolation;
import javax.swing.JSpinner;

public class MotionPanel extends JPanel implements MotionPlayer.Listener {
  private JTextField  txtSkelOut;
  private JTextField  txtModelOut;
  private JTextField  txtAddr;
  private JTextField  txtPort;

  private MainControl mainControl;
  private JLabel      lblInputFile;

  private JButton     btnPlayPause;
  private JSlider     motionSlider;

  MotionPlayer        motionPlayer;
  private JButton     btnSource;
  private JCheckBox   cbKinect;
  private JCheckBox   cbSendUDP;
  private JPanel      posesPanel;
  private JComboBox   cbPose;
  private JButton     btnSet;
  private JSpinner    lerpSpinner;
  private JButton     btnInterpolate;

  /**
   * Create the panel.
   */
  public MotionPanel(Config config, final Model model, final MainControl mainControl) {
    this.mainControl = mainControl;
    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[] { 0, 0 };
    gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
    gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
    gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
    setLayout(gridBagLayout);

    JPanel inputPanel = new JPanel();
    inputPanel.setBorder(new TitledBorder(null, "Input", TitledBorder.LEADING, TitledBorder.TOP,
        null, null));
    GridBagConstraints gbc_inputPanel = new GridBagConstraints();
    gbc_inputPanel.insets = new Insets(0, 0, 5, 0);
    gbc_inputPanel.fill = GridBagConstraints.BOTH;
    gbc_inputPanel.gridx = 0;
    gbc_inputPanel.gridy = 0;
    add(inputPanel, gbc_inputPanel);
    GridBagLayout gbl_inputPanel = new GridBagLayout();
    gbl_inputPanel.columnWidths = new int[] { 0, 0, 0 };
    gbl_inputPanel.rowHeights = new int[] { 0, 0, 0, 0 };
    gbl_inputPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
    gbl_inputPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
    inputPanel.setLayout(gbl_inputPanel);

    btnSource = new JButton("Motion:");
    btnSource.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {

        JFileChooser fc = new JFileChooser(MainApp.ROOT_DIR);
        if (fc.showOpenDialog(MotionPanel.this) == JFileChooser.APPROVE_OPTION) {
          File file = fc.getSelectedFile();
          lblInputFile.setText(file.getName());
          btnPlayPause.setEnabled(true);
          motionSlider.setEnabled(true);
          if (file.getName().endsWith(".ksm")) {
            MotionPanel.this.mainControl.getSkeletonPlayer().setInput(file);
            MotionPanel.this.motionPlayer = MotionPanel.this.mainControl.getSkeletonPlayer();
          } else {
            MotionPanel.this.mainControl.getModelPlayer().setInput(file);
            MotionPanel.this.motionPlayer = MotionPanel.this.mainControl.getModelPlayer();
          }
          motionSlider.setMinimum(0);
          motionSlider.setMaximum(MotionPanel.this.motionPlayer.getMaxFrames() - 1);
          motionSlider.setValue(0);
        }
      }
    });
    GridBagConstraints gbc_btnSource = new GridBagConstraints();
    gbc_btnSource.fill = GridBagConstraints.HORIZONTAL;
    gbc_btnSource.insets = new Insets(0, 0, 5, 5);
    gbc_btnSource.gridx = 0;
    gbc_btnSource.gridy = 0;
    inputPanel.add(btnSource, gbc_btnSource);

    lblInputFile = new JLabel("<none>");
    GridBagConstraints gbc_lblInputFile = new GridBagConstraints();
    gbc_lblInputFile.anchor = GridBagConstraints.WEST;
    gbc_lblInputFile.insets = new Insets(0, 0, 5, 0);
    gbc_lblInputFile.gridx = 1;
    gbc_lblInputFile.gridy = 0;
    inputPanel.add(lblInputFile, gbc_lblInputFile);

    btnPlayPause = new JButton("Play");
    btnPlayPause.setEnabled(false);
    btnPlayPause.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (motionPlayer.isPlaying()) {
          btnPlayPause.setText("Play");
          motionPlayer.pause();
          motionSlider.setEnabled(true);
        } else {
          motionPlayer.play();
          btnPlayPause.setText("Pause");
          motionSlider.setEnabled(false);
        }
      }
    });
    GridBagConstraints gbc_btnPlayPause = new GridBagConstraints();
    gbc_btnPlayPause.insets = new Insets(0, 0, 5, 5);
    gbc_btnPlayPause.fill = GridBagConstraints.BOTH;
    gbc_btnPlayPause.gridx = 0;
    gbc_btnPlayPause.gridy = 1;
    inputPanel.add(btnPlayPause, gbc_btnPlayPause);

    motionSlider = new JSlider();
    motionSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent arg0) {
        if (motionPlayer != null && !motionPlayer.isPlaying()) motionPlayer.setFrame(motionSlider
            .getValue());
      }
    });
    motionSlider.setEnabled(false);
    motionSlider.setValue(0);
    GridBagConstraints gbc_motionSlider = new GridBagConstraints();
    gbc_motionSlider.insets = new Insets(0, 0, 5, 0);
    gbc_motionSlider.fill = GridBagConstraints.HORIZONTAL;
    gbc_motionSlider.gridx = 1;
    gbc_motionSlider.gridy = 1;
    inputPanel.add(motionSlider, gbc_motionSlider);

    cbKinect = new JCheckBox("Use Kinect");
    cbKinect.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean useKinect = cbKinect.isSelected();

        if (useKinect && motionPlayer != null) {
          btnPlayPause.doClick();
        }

        btnSource.setEnabled(!useKinect);
        btnPlayPause.setEnabled(!useKinect && motionPlayer != null);
        motionSlider.setEnabled(!useKinect && motionPlayer != null);

        if (useKinect) {
          boolean active = MotionPanel.this.mainControl.getKinect().enable();
          if (!active) {
            cbKinect.setSelected(false);
            btnSource.setEnabled(true);
            btnPlayPause.setEnabled(motionPlayer != null);
            motionSlider.setEnabled(motionPlayer != null);
          }
        } else {
          MotionPanel.this.mainControl.getKinect().disable();
        }
      }
    });
    GridBagConstraints gbc_cbKinect = new GridBagConstraints();
    gbc_cbKinect.fill = GridBagConstraints.HORIZONTAL;
    gbc_cbKinect.insets = new Insets(0, 0, 10, 5);
    gbc_cbKinect.gridx = 0;
    gbc_cbKinect.gridy = 2;
    inputPanel.add(cbKinect, gbc_cbKinect);

    JPanel outputPanel = new JPanel();
    outputPanel.setBorder(new TitledBorder(null, "Output", TitledBorder.LEADING, TitledBorder.TOP,
        null, null));
    GridBagConstraints gbc_outputPanel = new GridBagConstraints();
    gbc_outputPanel.insets = new Insets(10, 0, 5, 0);
    gbc_outputPanel.fill = GridBagConstraints.BOTH;
    gbc_outputPanel.gridx = 0;
    gbc_outputPanel.gridy = 1;
    add(outputPanel, gbc_outputPanel);
    GridBagLayout gbl_outputPanel = new GridBagLayout();
    gbl_outputPanel.columnWidths = new int[] { 110, 0, 0 };
    gbl_outputPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
    gbl_outputPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
    gbl_outputPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
    outputPanel.setLayout(gbl_outputPanel);

    final JCheckBox chckbxActive = new JCheckBox("Skeleton");
    chckbxActive.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (chckbxActive.isSelected()) {
          MotionPanel.this.mainControl.getSkeletonRecorder().enable(txtSkelOut.getText());
          txtSkelOut.setEnabled(false);
        } else {
          txtSkelOut.setEnabled(true);
          MotionPanel.this.mainControl.getSkeletonRecorder().disable();
        }        
      }
    });
    GridBagConstraints gbc_chckbxActive = new GridBagConstraints();
    gbc_chckbxActive.fill = GridBagConstraints.HORIZONTAL;
    gbc_chckbxActive.insets = new Insets(0, 0, 5, 5);
    gbc_chckbxActive.gridx = 0;
    gbc_chckbxActive.gridy = 0;
    outputPanel.add(chckbxActive, gbc_chckbxActive);

    txtSkelOut = new JTextField(new File(MainApp.ROOT_DIR, "out.ksm").getAbsolutePath());
    GridBagConstraints gbc_txtSkelOut = new GridBagConstraints();
    gbc_txtSkelOut.insets = new Insets(0, 0, 5, 0);
    gbc_txtSkelOut.fill = GridBagConstraints.HORIZONTAL;
    gbc_txtSkelOut.gridx = 1;
    gbc_txtSkelOut.gridy = 0;
    outputPanel.add(txtSkelOut, gbc_txtSkelOut);
    txtSkelOut.setColumns(10);

    final JCheckBox chckbxModel = new JCheckBox("Model");
    chckbxModel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (((JCheckBox) e.getSource()).isSelected()) {
          MotionPanel.this.mainControl.getModelRecorder().enable(txtModelOut.getText());
          txtModelOut.setEnabled(false);
        } else {
          txtModelOut.setEnabled(true);
        }
      }
    });
    GridBagConstraints gbc_chckbxModel = new GridBagConstraints();
    gbc_chckbxModel.fill = GridBagConstraints.HORIZONTAL;
    gbc_chckbxModel.insets = new Insets(0, 0, 5, 5);
    gbc_chckbxModel.gridx = 0;
    gbc_chckbxModel.gridy = 1;
    outputPanel.add(chckbxModel, gbc_chckbxModel);

    txtModelOut = new JTextField(new File(MainApp.ROOT_DIR, "out.kmm").getAbsolutePath());
    GridBagConstraints gbc_txtModelOut = new GridBagConstraints();
    gbc_txtModelOut.insets = new Insets(0, 0, 5, 0);
    gbc_txtModelOut.fill = GridBagConstraints.HORIZONTAL;
    gbc_txtModelOut.gridx = 1;
    gbc_txtModelOut.gridy = 1;
    outputPanel.add(txtModelOut, gbc_txtModelOut);
    txtModelOut.setColumns(10);

    cbSendUDP = new JCheckBox("Send UDP");
    cbSendUDP.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MotionPanel.this.mainControl.getModelMessenger().setHost(txtAddr.getText(),
            Integer.parseInt(txtPort.getText()));
        MotionPanel.this.mainControl.getModelMessenger().setActive(cbSendUDP.isSelected());

        txtAddr.setEnabled(!cbSendUDP.isSelected());
        txtPort.setEnabled(!cbSendUDP.isSelected());
      }
    });
    GridBagConstraints gbc_cbSendUDP = new GridBagConstraints();
    gbc_cbSendUDP.fill = GridBagConstraints.HORIZONTAL;
    gbc_cbSendUDP.insets = new Insets(0, 0, 5, 5);
    gbc_cbSendUDP.gridx = 0;
    gbc_cbSendUDP.gridy = 3;
    outputPanel.add(cbSendUDP, gbc_cbSendUDP);

    JLabel lblHostAddress = new JLabel("Host Address:");
    GridBagConstraints gbc_lblHostAddress = new GridBagConstraints();
    gbc_lblHostAddress.anchor = GridBagConstraints.EAST;
    gbc_lblHostAddress.insets = new Insets(0, 0, 5, 5);
    gbc_lblHostAddress.gridx = 0;
    gbc_lblHostAddress.gridy = 4;
    outputPanel.add(lblHostAddress, gbc_lblHostAddress);

    txtAddr = new JTextField();
    txtAddr.setText(config.sendAddress);
    GridBagConstraints gbc_txtAddr = new GridBagConstraints();
    gbc_txtAddr.insets = new Insets(0, 0, 5, 0);
    gbc_txtAddr.fill = GridBagConstraints.HORIZONTAL;
    gbc_txtAddr.gridx = 1;
    gbc_txtAddr.gridy = 4;
    outputPanel.add(txtAddr, gbc_txtAddr);
    txtAddr.setColumns(10);

    JLabel lblHostPort = new JLabel("Host Port:");
    GridBagConstraints gbc_lblHostPort = new GridBagConstraints();
    gbc_lblHostPort.anchor = GridBagConstraints.EAST;
    gbc_lblHostPort.insets = new Insets(0, 0, 0, 5);
    gbc_lblHostPort.gridx = 0;
    gbc_lblHostPort.gridy = 5;
    outputPanel.add(lblHostPort, gbc_lblHostPort);

    txtPort = new JTextField();
    txtPort.setText(config.sendPort + "");
    GridBagConstraints gbc_txtPort = new GridBagConstraints();
    gbc_txtPort.fill = GridBagConstraints.HORIZONTAL;
    gbc_txtPort.gridx = 1;
    gbc_txtPort.gridy = 5;
    outputPanel.add(txtPort, gbc_txtPort);
    txtPort.setColumns(10);

    mainControl.getModelPlayer().addListener(this);
    mainControl.getSkeletonPlayer().addListener(this);

    posesPanel = new JPanel();
    posesPanel.setBorder(new TitledBorder(null, "Poses", TitledBorder.LEADING, TitledBorder.TOP,
        null, null));
    GridBagConstraints gbc_posesPanel = new GridBagConstraints();
    gbc_posesPanel.fill = GridBagConstraints.BOTH;
    gbc_posesPanel.gridx = 0;
    gbc_posesPanel.gridy = 2;
    add(posesPanel, gbc_posesPanel);
    GridBagLayout gbl_posesPanel = new GridBagLayout();
    gbl_posesPanel.columnWidths = new int[] { 0, 0, 0 };
    gbl_posesPanel.rowHeights = new int[] { 0, 0, 0 };
    gbl_posesPanel.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
    gbl_posesPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
    posesPanel.setLayout(gbl_posesPanel);

    cbPose = new JComboBox();
    if (model.poses == null) {
      cbPose.setEnabled(false);
    } else {
      for (ModelPose pose : model.poses)
        cbPose.addItem(pose);
      cbPose.setSelectedIndex(cbPose.getItemCount() - 1);
    }

    GridBagConstraints gbc_comboBox = new GridBagConstraints();
    gbc_comboBox.insets = new Insets(0, 0, 5, 5);
    gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
    gbc_comboBox.gridx = 0;
    gbc_comboBox.gridy = 0;
    posesPanel.add(cbPose, gbc_comboBox);

    btnSet = new JButton("Set");
    btnSet.setEnabled(model.poses != null);
    btnSet.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ((ModelPose) cbPose.getSelectedItem()).apply(model);
        mainControl.getGlViews().get("Robot View").repaint();
        model.commitUpdate();
      }
    });

    if (btnSet.isEnabled()) btnSet.doClick();

    GridBagConstraints gbc_btnApply = new GridBagConstraints();
    gbc_btnApply.fill = GridBagConstraints.HORIZONTAL;
    gbc_btnApply.insets = new Insets(0, 0, 5, 0);
    gbc_btnApply.gridx = 1;
    gbc_btnApply.gridy = 0;
    posesPanel.add(btnSet, gbc_btnApply);

    lerpSpinner = new JSpinner();
    lerpSpinner.setValue(1000);
    GridBagConstraints gbc_lerpSpinner = new GridBagConstraints();
    gbc_lerpSpinner.fill = GridBagConstraints.HORIZONTAL;
    gbc_lerpSpinner.insets = new Insets(0, 0, 0, 5);
    gbc_lerpSpinner.gridx = 0;
    gbc_lerpSpinner.gridy = 1;
    posesPanel.add(lerpSpinner, gbc_lerpSpinner);

    btnInterpolate = new JButton("Interpolate");
    btnInterpolate.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        final ModelPose pose = (ModelPose) cbPose.getSelectedItem();
        final int durationMS = ((Integer) lerpSpinner.getValue());
        new Thread(new Runnable() {
          public void run() {
            PoseInterpolation animation = new PoseInterpolation(model, pose, durationMS, 20);
            while (!animation.isFinished()) {
              animation.update();
              mainControl.getGlViews().get("Robot View").repaint();
              try {
                Thread.sleep(20);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
          }
        }).start();
      }
    });
    GridBagConstraints gbc_btnInterpolate = new GridBagConstraints();
    gbc_btnInterpolate.fill = GridBagConstraints.HORIZONTAL;
    gbc_btnInterpolate.gridx = 1;
    gbc_btnInterpolate.gridy = 1;
    posesPanel.add(btnInterpolate, gbc_btnInterpolate);
  }

  @Override
  public void frameChanged(MotionPlayer player) {
    this.motionSlider.setValue(player.getCurFrame());
  }

  @Override
  public void motionEnded(MotionPlayer player) {
    btnPlayPause.setText("Play");
    motionSlider.setEnabled(true);
  }
}
