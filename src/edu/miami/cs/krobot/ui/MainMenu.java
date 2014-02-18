package edu.miami.cs.krobot.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import edu.miami.cs.krobot.Config;
import edu.miami.cs.krobot.MainControl;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.view.RobotView;
import edu.miami.cs.krobot.view.ScreenLayout;
import edu.miami.cs.krobot.view.ScreenLayout.Position;

public class MainMenu extends JMenuBar {

  MainControl mainControl;
  JMenu       viewMenu;

  public MainMenu(Model model, MainControl mvc, Config config) {
    this.mainControl = mvc;
    initViewMenu(config);
  }

  private void initViewMenu(Config config) {
    viewMenu = new JMenu("View");

    {
      JMenu singleView = new JMenu("One View");
      JMenuItem robotOnly = new JMenuItem("Robot Model");
      robotOnly.addActionListener(new ViewSetter(new String[] { "Robot View" },
          new Position[] { Position.CENTER }));
      singleView.add(robotOnly);
      JMenuItem skeletonOnly = new JMenuItem("User Skeleton");
      skeletonOnly.addActionListener(new ViewSetter(new String[] { "Skeleton View" },
          new Position[] { Position.CENTER }));
      singleView.add(skeletonOnly);
      JMenuItem depthOnly = new JMenuItem("Depth");
      depthOnly.addActionListener(new ViewSetter(new String[] { "Depth View" },
          new Position[] { Position.CENTER }));
      singleView.add(depthOnly);
      viewMenu.add(singleView);
    }

    {
      JMenu doubleView = new JMenu("Two Views");
      JMenuItem robotSkeleton = new JMenuItem("Robot & Skeleton");
      robotSkeleton.addActionListener(new ViewSetter(
          new String[] { "Robot View", "Skeleton View" }, new Position[] { Position.LEFT,
              Position.RIGHT }));
      doubleView.add(robotSkeleton);
      JMenuItem robotDepth = new JMenuItem("Robot & Depth");
      robotDepth.addActionListener(new ViewSetter(new String[] { "Robot View", "Depth View" },
          new Position[] { Position.LEFT, Position.RIGHT }));
      doubleView.add(robotDepth);
      JMenuItem skeletonDepth = new JMenuItem("Skeleton & Depth");
      skeletonDepth.addActionListener(new ViewSetter(
          new String[] { "Skeleton View", "Depth View" }, new Position[] { Position.LEFT,
              Position.RIGHT }));
      doubleView.add(skeletonDepth);
      viewMenu.add(doubleView);
    }

    JMenuItem allViews = new JMenuItem("All Views");
    allViews
        .addActionListener(new ViewSetter(new String[] { "Robot View", "Skeleton View",
            "Depth View" }, new Position[] { Position.LEFT, Position.BOTTOM_RIGHT,
            Position.TOP_RIGHT }));
    viewMenu.add(allViews);

    add(viewMenu);
  }

  private class ViewSetter implements ActionListener {

    String[]                views;
    ScreenLayout.Position[] positions;

    public ViewSetter(String[] views, ScreenLayout.Position[] positions) {
      this.views = views;
      this.positions = positions;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      mainControl.getMasterView().clearViews();
      for (int i = 0; i < views.length; i++)
        mainControl.getMasterView().setView(mainControl.getGlViews().get(views[i]), positions[i]);
      mainControl.getCanvas().repaint();
    }
  }
}
