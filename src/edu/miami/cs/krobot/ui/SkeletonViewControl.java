package edu.miami.cs.krobot.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import edu.miami.cs.krobot.view.SkeletonView;

public class SkeletonViewControl implements MouseController {

  SkeletonView  view;
  CamController camController;
  boolean       mouseActive = false;

  public SkeletonViewControl(SkeletonView view) {
    this.view = view;
    camController = new CamController(view.getCamera());
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (mouseActive) {
      camController.mouseDragged(e);
      view.repaint();
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    if (containsMouse(e.getPoint())) {
      camController.mouseMoved(e);
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (mouseActive || containsMouse(e.getPoint())) {
      camController.mouseClicked(e);
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (containsMouse(e.getPoint())) {
      camController.mousePressed(e);
      mouseActive = true;
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (mouseActive || containsMouse(e.getPoint())) {
      camController.mouseReleased(e);
      mouseActive = false;
    }
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (mouseActive || containsMouse(e.getPoint())) {
      camController.mouseWheelMoved(e);
      view.repaint();
    }
  }

  boolean containsMouse(Point p) {
    return view.getViewport() != null && view.getViewport().contains(p);
  }
}
