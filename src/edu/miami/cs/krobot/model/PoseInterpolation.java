package edu.miami.cs.krobot.model;

import java.util.Map.Entry;
import edu.miami.cs.krobot.model.joint.Joint1DOF;

public class PoseInterpolation {

  final Model       model;
  final Joint1DOF[] joints;
  final float[]     deltas;
  final int         totalFrames;
  int               frame    = 0;
  boolean           finished = false;

  public PoseInterpolation(Model model, ModelPose targetPose, int durationMS, int frameMS) {
    this.model = model;

    totalFrames = durationMS / frameMS;

    joints = new Joint1DOF[targetPose.values.size()];
    deltas = new float[joints.length];

    int i = 0;
    for (Entry<Joint1DOF, Number> jointMapping : targetPose.values.entrySet()) {
      Joint1DOF joint = jointMapping.getKey();
      float targetValue = jointMapping.getValue().floatValue();
      float currentValue = ((Joint1DOF) model.getPart(joint.name)).getValue();
      deltas[i] = (targetValue - currentValue) / totalFrames;
      joints[i] = joint;
      i++;
    }
  }

  public void update() {
    if (finished) return;

    for (int i = 0; i < joints.length; i++)
      joints[i].setValue(joints[i].getValue() + deltas[i]);
    
    model.commitUpdate();

    frame++;
    if (frame == totalFrames) finished = true;
  }

  public boolean isFinished() {
    return finished;
  }
}
