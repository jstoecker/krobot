package edu.miami.cs.krobot.openni;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.StatusException;
import edu.miami.cs.js.math.vector.Orientation;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.Config;
import edu.miami.cs.krobot.MotionPlayer;

/**
 * Plays back a recorded motion file.
 * 
 * @author justin
 */
public class NiSkeletonPlayer extends NiSkeletonSource implements MotionPlayer {

  Config                           config;
  private boolean                  playing   = false;
  private NiSkeleton               skeleton;
  ArrayList<SkeletonJoint>         jointIds  = new ArrayList<SkeletonJoint>();
  private PlayThread               playThread;
  int                              delay     = 30;
  int                              curFrame  = 0;
  ArrayList<Frame>                 frames;
  ArrayList<MotionPlayer.Listener> listeners = new ArrayList<MotionPlayer.Listener>();

  public NiSkeletonPlayer(Config config) {
    this.config = config;

    jointIds.add(SkeletonJoint.HEAD);
    jointIds.add(SkeletonJoint.NECK);
    jointIds.add(SkeletonJoint.TORSO);
    jointIds.add(SkeletonJoint.LEFT_SHOULDER);
    jointIds.add(SkeletonJoint.LEFT_ELBOW);
    jointIds.add(SkeletonJoint.LEFT_HAND);
    jointIds.add(SkeletonJoint.RIGHT_SHOULDER);
    jointIds.add(SkeletonJoint.RIGHT_ELBOW);
    jointIds.add(SkeletonJoint.RIGHT_HAND);
    jointIds.add(SkeletonJoint.LEFT_HIP);
    jointIds.add(SkeletonJoint.LEFT_KNEE);
    jointIds.add(SkeletonJoint.LEFT_FOOT);
    jointIds.add(SkeletonJoint.RIGHT_HIP);
    jointIds.add(SkeletonJoint.RIGHT_KNEE);
    jointIds.add(SkeletonJoint.RIGHT_FOOT);

  }

  public void setInput(File file) {
    try {
      if (file == null) {
        System.out.println("Cannot read file");
        return;
      }
      
      BufferedReader reader = new BufferedReader(new FileReader(file));

      skeleton = new NiSkeleton(0, jointIds, config);
      curFrame = 0;
      frames = new ArrayList<Frame>();
      
      String line = null;
      while ((line = reader.readLine()) != null)
        frames.add(new Frame(new StringTokenizer(line)));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void play() {
    if (playing) return;
    playing = true;
    if (curFrame == frames.size()) curFrame = 0;
    playThread = new PlayThread();
    playThread.start();
  }
  
  @Override
  public void pause() {
    playing = false;
  }

  @Override
  public boolean isPlaying() {
    return playing;
  }

  @Override
  public int getCurFrame() {
    return curFrame;
  }

  @Override
  public int getMaxFrames() {
    return frames.size();
  }
  
  private Vec3 readVec3(StringTokenizer st) {
    try {
      float x = Float.parseFloat(st.nextToken());
      float y = Float.parseFloat(st.nextToken());
      float z = Float.parseFloat(st.nextToken());

      if (Float.compare(x, Float.NaN) == 0 || Float.compare(y, Float.NaN) == 0
          || Float.compare(z, Float.NaN) == 0) return null;

      return new Vec3(x, y, z);
    } catch (Exception e) {
      return null;
    }
  }

  private void readJoint(NiJoint joint, StringTokenizer st) throws StatusException {
    joint.setPositionRaw(readVec3(st));

    Vec3 f = readVec3(st);
    Vec3 u = readVec3(st);
    Vec3 r = readVec3(st);
    if (f == null || u == null || r == null)
      joint.orientation = null;
    else
      joint.orientation = new Orientation(f, u, r);
  }

  @Override
  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  @Override
  public void setFrame(int frameIndex) {
    curFrame = frameIndex;
    frames.get(curFrame).set();
  }
  
  private class PlayThread extends Thread {
    public void run() {
      while (playing) {

        frames.get(curFrame++).set();
        
        for (MotionPlayer.Listener l : listeners) {
          l.frameChanged(NiSkeletonPlayer.this);
        }

        if (curFrame == frames.size()) {
          playing = false;
          for (MotionPlayer.Listener l : listeners) {
            l.motionEnded(NiSkeletonPlayer.this);
          }
        } else {
          try {
            Thread.sleep(delay);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private class Frame {
    Vec3[]        jointPositions;
    Orientation[] jointOrientations;

    public Frame(StringTokenizer st) {
      jointPositions = new Vec3[jointIds.size()];
      jointOrientations = new Orientation[jointIds.size()];

      for (int i = 0; i < jointPositions.length; i++) {
        jointPositions[i] = readVec3(st);

        Vec3 f = readVec3(st);
        Vec3 u = readVec3(st);
        Vec3 r = readVec3(st);
        if (f == null || u == null || r == null)
          jointOrientations[i] = null;
        else
          jointOrientations[i] = new Orientation(f, u, r);
      }
    }

    void set() {
      for (int i = 0; i < jointPositions.length; i++) {
        skeleton.getJoints().get(i).setPositionRaw(jointPositions[i]);
        skeleton.getJoints().get(i).orientation = jointOrientations[i];
      }
      skeleton.updateOrientation();

      for (NiSkeletonSrcListener l : skeletonListeners)
        l.skeletonUpdated(skeleton);
      
    }
  }
}
