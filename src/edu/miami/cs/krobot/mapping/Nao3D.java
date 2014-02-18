package edu.miami.cs.krobot.mapping;

import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.joint.RevoluteJoint;

/**
 * Nao model used in RoboCup soccer. This is a wrapper/specialization of the base model class and
 * merely provides more convenient members for the mapper.
 * 
 * @author justin
 */
public class Nao3D {

  Model         model;
  RevoluteJoint neckPitch;
  RevoluteJoint neckYaw;
  RevoluteJoint rShoulderPitch;
  RevoluteJoint rShoulderYaw;
  RevoluteJoint rArmRoll;
  RevoluteJoint rArmYaw;
  RevoluteJoint rHipYawPitch;
  RevoluteJoint rHipRoll;
  RevoluteJoint rHipPitch;
  RevoluteJoint rKneePitch;
  RevoluteJoint rFootPitch;
  RevoluteJoint rFootRoll;
  RevoluteJoint lShoulderPitch;
  RevoluteJoint lShoulderYaw;
  RevoluteJoint lArmRoll;
  RevoluteJoint lArmYaw;
  RevoluteJoint lHipYawPitch;
  RevoluteJoint lHipRoll;
  RevoluteJoint lHipPitch;
  RevoluteJoint lKneePitch;
  RevoluteJoint lFootPitch;
  RevoluteJoint lFootRoll;

  public Nao3D(Model model) {
    this.model = model;

    neckPitch = (RevoluteJoint) model.getPart("Neck Pitch");
    neckYaw = (RevoluteJoint) model.getPart("Neck Yaw");
    rShoulderPitch = (RevoluteJoint) model.getPart("Right Shoulder Pitch");
    rShoulderYaw = (RevoluteJoint) model.getPart("Right Shoulder Yaw");
    rArmRoll = (RevoluteJoint) model.getPart("Right Arm Roll");
    rArmYaw = (RevoluteJoint) model.getPart("Right Arm Yaw");
    rHipYawPitch = (RevoluteJoint) model.getPart("Right Hip Yaw/Pitch");
    rHipRoll = (RevoluteJoint) model.getPart("Right Hip Roll");
    rHipPitch = (RevoluteJoint) model.getPart("Right Hip Pitch");
    rKneePitch = (RevoluteJoint) model.getPart("Right Knee Pitch");
    rFootPitch = (RevoluteJoint) model.getPart("Right Foot Pitch");
    rFootRoll = (RevoluteJoint) model.getPart("Right Foot Roll");
    lShoulderPitch = (RevoluteJoint) model.getPart("Left Shoulder Pitch");
    lShoulderYaw = (RevoluteJoint) model.getPart("Left Shoulder Yaw");
    lArmRoll = (RevoluteJoint) model.getPart("Left Arm Roll");
    lArmYaw = (RevoluteJoint) model.getPart("Left Arm Yaw");
    lHipYawPitch = (RevoluteJoint) model.getPart("Left Hip Yaw/Pitch");
    lHipRoll = (RevoluteJoint) model.getPart("Left Hip Roll");
    lHipPitch = (RevoluteJoint) model.getPart("Left Hip Pitch");
    lKneePitch = (RevoluteJoint) model.getPart("Left Knee Pitch");
    lFootPitch = (RevoluteJoint) model.getPart("Left Foot Pitch");
    lFootRoll = (RevoluteJoint) model.getPart("Left Foot Roll");
  }
}
