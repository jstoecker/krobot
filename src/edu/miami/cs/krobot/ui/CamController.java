package edu.miami.cs.krobot.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import edu.miami.cs.js.jgloo.view.OrbitCamera;

public class CamController implements MouseController {

  private OrbitCamera camera;
  int                 clickX, clickY;
  float               clickAzi, clickAlt;

  public CamController(OrbitCamera camera) {
    this.camera = camera;
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getWheelRotation() > 0) {
      camera.setRadius(camera.getRadius() + 0.2f);
    } else {
      camera.setRadius(camera.getRadius() - 0.2f);
    }
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    camera.setAzimuth(clickAzi + (float) (-(e.getX() - clickX) * Math.PI * 0.002));
    camera.setAltitude(clickAlt + (float) ((e.getY() - clickY) * Math.PI * 0.002));
  }

  @Override
  public void mouseMoved(MouseEvent e) {
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
    clickX = e.getX();
    clickY = e.getY();
    clickAzi = camera.getAzimuth();
    clickAlt = camera.getAltitude();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }
}
