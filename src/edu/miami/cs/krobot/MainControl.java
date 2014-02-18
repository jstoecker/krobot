package edu.miami.cs.krobot;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.media.opengl.awt.GLCanvas;
import edu.miami.cs.krobot.mapping.NiSkeletonMapper;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.ModelMessenger;
import edu.miami.cs.krobot.model.ModelPlayer;
import edu.miami.cs.krobot.model.ModelRecorder;
import edu.miami.cs.krobot.openni.KinectSensor;
import edu.miami.cs.krobot.openni.NiSkeleton;
import edu.miami.cs.krobot.openni.NiSkeletonPlayer;
import edu.miami.cs.krobot.openni.NiSkeletonRecorder;
import edu.miami.cs.krobot.openni.NiSkeletonSource;
import edu.miami.cs.krobot.openni.NiSkeletonSrcListener;
import edu.miami.cs.krobot.ui.MouseController;
import edu.miami.cs.krobot.ui.RobotViewControl;
import edu.miami.cs.krobot.ui.SkeletonViewControl;
import edu.miami.cs.krobot.view.DepthView;
import edu.miami.cs.krobot.view.GLView;
import edu.miami.cs.krobot.view.MasterView;
import edu.miami.cs.krobot.view.RobotView;
import edu.miami.cs.krobot.view.ScreenLayout.Position;
import edu.miami.cs.krobot.view.SkeletonView;

public class MainControl implements MouseController {

  Config                     config;
  final GLCanvas             canvas;
  MasterView                 masterView;
  ArrayList<MouseController> mouseControllers  = new ArrayList<MouseController>();
  HashMap<String, GLView>    glViews           = new HashMap<String, GLView>();

  Model                      model;

  NiSkeletonSource           activeSkeletonSource;
  ModelPlayer                motionPlayer;
  NiSkeletonPlayer           skeletonPlayer;
  ModelRecorder              modelRecorder;
  ModelMessenger             modelMessenger;
  NiSkeletonRecorder         skeletonRecorder;
  KinectSensor               kinect;

  RobotView                  robotView;
  SkeletonView               skeletonView;
  DepthView                  depthView;

  SkeletonSourceUpdater      skelSourceUpdater = new SkeletonSourceUpdater();

  public MainControl(Model model, GLCanvas canvas, Config config) {
    this.model = model;
    this.canvas = canvas;
    this.config = config;

    kinect = new KinectSensor(config);
    skeletonPlayer = new NiSkeletonPlayer(config);
    motionPlayer = new ModelPlayer(model, config);
    modelRecorder = new ModelRecorder(model);
    skeletonRecorder = new NiSkeletonRecorder();
    modelMessenger = new ModelMessenger(model, config.sendAddress, config.sendPort);

    initViews();

    kinect.addListener(model.getMapper());
    kinect.addListener(skelSourceUpdater);
    skeletonPlayer.addListener(skelSourceUpdater);

    mouseControllers.add(new RobotViewControl(robotView));
    mouseControllers.add(new SkeletonViewControl(skeletonView));
    canvas.addGLEventListener(masterView);
    canvas.addMouseListener(this);
    canvas.addMouseMotionListener(this);
    canvas.addMouseWheelListener(this);
  }

  private void initViews() {
    masterView = new MasterView(model);
    masterView.setCanvas(canvas);

    robotView = new RobotView("Robot View", model, masterView, config);
    glViews.put(robotView.getName(), robotView);
    masterView.setView(robotView, Position.CENTER);

    skeletonView = new SkeletonView("Skeleton View", masterView);
    glViews.put(skeletonView.getName(), skeletonView);
    // masterView.setView(skeletonView, Position.BOTTOM_RIGHT);

    depthView = new DepthView("Depth View", masterView, kinect);
    kinect.addListener(depthView);
    glViews.put(depthView.getName(), depthView);
    // masterView.setView(depthView, Position.TOP_RIGHT);
  }

  public ArrayList<MouseController> getMouseControllers() {
    return mouseControllers;
  }

  public KinectSensor getKinect() {
    return kinect;
  }

  public ModelMessenger getModelMessenger() {
    return modelMessenger;
  }

  public MasterView getMasterView() {
    return masterView;
  }

  public HashMap<String, GLView> getGlViews() {
    return glViews;
  }

  public GLCanvas getCanvas() {
    return canvas;
  }

  public ModelRecorder getModelRecorder() {
    return modelRecorder;
  }

  public NiSkeletonRecorder getSkeletonRecorder() {
    return skeletonRecorder;
  }

  public ModelPlayer getModelPlayer() {
    return motionPlayer;
  }

  public NiSkeletonPlayer getSkeletonPlayer() {
    return skeletonPlayer;
  }

  public void shutdown() {
    kinect.disable();
  }

  public void addView(GLView view) {
    glViews.put(view.getName(), view);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    for (MouseController mc : mouseControllers)
      mc.mouseClicked(e);
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    for (MouseController mc : mouseControllers)
      mc.mouseEntered(e);
  }

  @Override
  public void mouseExited(MouseEvent e) {
    for (MouseController mc : mouseControllers)
      mc.mouseExited(e);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    for (MouseController mc : mouseControllers)
      mc.mousePressed(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    for (MouseController mc : mouseControllers)
      mc.mouseReleased(e);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    for (MouseController mc : mouseControllers)
      mc.mouseDragged(e);
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    for (MouseController mc : mouseControllers)
      mc.mouseMoved(e);
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    for (MouseController mc : mouseControllers)
      mc.mouseWheelMoved(e);
  }

  private class SkeletonSourceUpdater implements NiSkeletonSrcListener {
    public void skeletonUpdated(NiSkeleton skeleton) {
      skeletonView.skeletonUpdated(skeleton);
      model.getMapper().skeletonUpdated(skeleton);
      skeletonRecorder.skeletonUpdated(skeleton);
      canvas.repaint();
    }
  }
}
