package edu.miami.cs.krobot.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import edu.miami.cs.js.math.vector.Orientation;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.Config;
import edu.miami.cs.krobot.MotionPlayer;

public class ModelPlayer implements MotionPlayer {

  Config                           config;
  private boolean                  playing   = false;
  private PlayThread               playThread;
  int                              delay     = 35;
  private Model                    model;
  int                              curFrame  = 0;
  ArrayList<Frame>                 frames;
  ArrayList<MotionPlayer.Listener> listeners = new ArrayList<MotionPlayer.Listener>();

  public ModelPlayer(Model model, Config config) {
    this.config = config;
    this.model = model;
  }

  public void setInput(File file) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
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

  @Override
  public void setFrame(int frameIndex) {
    curFrame = frameIndex;
    frames.get(curFrame).set();
  }

  @Override
  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  private static Vec3 parseVec3(StringTokenizer st) {
    try {
      float x = Float.parseFloat(st.nextToken());
      float y = Float.parseFloat(st.nextToken());
      float z = Float.parseFloat(st.nextToken());
      return new Vec3(x, y, z);
    } catch (Exception e) {
      return null;
    }
  }

  private class PlayThread extends Thread {
    public void run() {
      while (playing) {
        frames.get(curFrame++).set();
        
        for (MotionPlayer.Listener l : listeners) {
          l.frameChanged(ModelPlayer.this);
        }

        if (curFrame == frames.size()) {
          playing = false;
          for (MotionPlayer.Listener l : listeners) {
            l.motionEnded(ModelPlayer.this);
          }
        } else {
          try {
            Thread.sleep((int) frames.get(curFrame).time);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private class Frame {

    float       time;
    float[]     jointValues;
    Orientation orientation;

    public Frame(StringTokenizer st) {

      time = Float.parseFloat(st.nextToken());

      jointValues = new float[model.getJoints().size()];
      for (int i = 0; i < jointValues.length; i++)
        jointValues[i] = Float.parseFloat(st.nextToken());

      Vec3 u = parseVec3(st);
      Vec3 f = parseVec3(st);
      if (u != null && f != null)
        orientation = new Orientation(f, u, f.cross(u).normalize());
      else
        orientation = null;
    }

    void set() {
      for (int i = 0; i < jointValues.length; i++)
        model.getJoints().get(i).setValue(jointValues[i]);
      model.setOrientation(orientation);
    }
  }

}
