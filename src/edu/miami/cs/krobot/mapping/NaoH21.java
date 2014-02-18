package edu.miami.cs.krobot.mapping;

import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.joint.RevoluteJoint;

public class NaoH21 {

  Model         model;
  RevoluteJoint headYaw;
  RevoluteJoint headPitch;
  RevoluteJoint lShoulderPitch;
  RevoluteJoint lShoulderRoll;
  RevoluteJoint lElbowYaw;
  RevoluteJoint lElbowRoll;
  RevoluteJoint rShoulderPitch;
  RevoluteJoint rShoulderRoll;
  RevoluteJoint rElbowYaw;
  RevoluteJoint rElbowRoll;
  RevoluteJoint lHipYawPitch;
  RevoluteJoint lHipRoll;
  RevoluteJoint lHipPitch;
  RevoluteJoint lKneePitch;
  RevoluteJoint lAnklePitch;
  RevoluteJoint lAnkleRoll;
  RevoluteJoint rHipYawPitch;
  RevoluteJoint rHipRoll;
  RevoluteJoint rHipPitch;
  RevoluteJoint rKneePitch;
  RevoluteJoint rAnklePitch;
  RevoluteJoint rAnkleRoll;

  public NaoH21(Model model) {
    this.model = model;

    headYaw = (RevoluteJoint) model.getPart("HeadYaw");
    headPitch = (RevoluteJoint) model.getPart("HeadPitch");
    lShoulderPitch = (RevoluteJoint) model.getPart("LShoulderPitch");
    lShoulderRoll = (RevoluteJoint) model.getPart("LShoulderRoll");
    lElbowYaw = (RevoluteJoint) model.getPart("LElbowYaw");
    lElbowRoll = (RevoluteJoint) model.getPart("LElbowRoll");
    rShoulderPitch = (RevoluteJoint) model.getPart("RShoulderPitch");
    rShoulderRoll = (RevoluteJoint) model.getPart("RShoulderRoll");
    rElbowYaw = (RevoluteJoint) model.getPart("RElbowYaw");
    rElbowRoll = (RevoluteJoint) model.getPart("RElbowRoll");
    lHipYawPitch = (RevoluteJoint) model.getPart("LHipYawPitch");
    lHipRoll = (RevoluteJoint) model.getPart("LHipRoll");
    lHipPitch = (RevoluteJoint) model.getPart("LHipPitch");
    lKneePitch = (RevoluteJoint) model.getPart("LKneePitch");
    lAnklePitch = (RevoluteJoint) model.getPart("LAnklePitch");
    lAnkleRoll = (RevoluteJoint) model.getPart("LAnkleRoll");
    rHipYawPitch = (RevoluteJoint) model.getPart("RHipYawPitch");
    rHipRoll = (RevoluteJoint) model.getPart("RHipRoll");
    rHipPitch = (RevoluteJoint) model.getPart("RHipPitch");
    rKneePitch = (RevoluteJoint) model.getPart("RKneePitch");
    rAnklePitch = (RevoluteJoint) model.getPart("RAnklePitch");
    rAnkleRoll = (RevoluteJoint) model.getPart("RAnkleRoll");
  }
}
