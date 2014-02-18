package edu.miami.cs.krobot;

public interface MotionPlayer {

  interface Listener {
    void frameChanged(MotionPlayer player);
    void motionEnded(MotionPlayer player);
  }
  
  boolean isPlaying();

  void play();

  void pause();
  
  int getCurFrame();
  
  int getMaxFrames();
  
  void setFrame(int frameIndex);
  
  void addListener(MotionPlayer.Listener listener);
}
