package edu.miami.cs.krobot.mapping;

import java.util.HashMap;
import org.OpenNI.SkeletonJoint;
import edu.miami.cs.js.math.util.GMath;
import edu.miami.cs.js.math.vector.Mat4;
import edu.miami.cs.js.math.vector.Orientation;
import edu.miami.cs.js.math.vector.ReadableVec3;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.mapping.NaoH21HipTable.HipConfig;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.joint.Joint1DOF;
import edu.miami.cs.krobot.model.joint.RevoluteJoint;
import edu.miami.cs.krobot.openni.NiSkeleton;
import edu.miami.cs.krobot.util.IIRFilter;
import edu.miami.cs.krobot.util.Maths;

/**0
 * Maps an NiSkeleton to a NAO v4 H21 robot model.
 * 
 * @author justin
 */
public class NaoH21Mapper extends NiSkeletonMapper {

  // Coefficients for a 2nd order Butterworth low-pass filter with cutoff of 1/10 the sampling rate.
  private static final double[]         BUTTER_B = { 0.0675, 0.1349, 0.0675 };
  private static final double[]         BUTTER_A = { 1.0000, -1.1430, 0.4128 };
  private NaoH21                        nao;
  private HashMap<Joint1DOF, IIRFilter> filters;
  private NaoH21HipTable                hipLookup;
  private Mat4                          skeletonToModelSpace;
  private Vec3                          prevLLegNormal;
  private Vec3                          prevRLegNormal;

  @Override
  public void initialize(Model model) {
    super.initialize(model);
    nao = new NaoH21(model);
    
    // Construct the leg lookup table and then reset the joint angles to 0.
    hipLookup = NaoH21HipTable.build(nao, 1);
    for (Joint1DOF joint : model.getJoints())
      joint.setValue(0);

    // Initialize new IIR filters for each joint.
    filters = new HashMap<Joint1DOF, IIRFilter>();
    for (Joint1DOF joint : model.getJoints())
      filters.put(joint, new IIRFilter(BUTTER_B, BUTTER_A));
    
    // Transforms skeleton space coordinates to robot model space coordinates.
    Mat4 r1 = Mat4.createRotationZ(-Math.PI / 2);
    Mat4 r2 = Mat4.createRotationX(Math.PI / 2);
    skeletonToModelSpace = r1.times(r2);
  }

  @Override
  public void map(NiSkeleton skeleton) {
    // The arms are mapped using the upper torso as a frame of reference.
    Orientation o = skeleton.getUpperOrientation();
    if (o == null) return;
    calcRightArm(skeleton, o.getForward(), o.getUp(), o.getRight());
    calcLeftArm(skeleton, o.getForward(), o.getUp(), o.getRight());

    o = skeleton.getLowerOrientation();
    if (o == null) return;
    calcRightLeg(skeleton, o.getForward(), o.getUp(), o.getRight());
    calcLeftLeg(skeleton, o.getForward(), o.getUp(), o.getRight());
  }

  private void calcRightArm(NiSkeleton skeleton, ReadableVec3 f, ReadableVec3 u, ReadableVec3 r) {
    if (f == null || u == null || r == null) return;

    Vec3 bicep = skeleton.getVector(SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.RIGHT_ELBOW);
    if (bicep == null) return;
    Vec3 bicepProjected = Maths.project(bicep, f, u);
    Vec3 bicepNormal = bicep.cross(bicepProjected).normalize();

    Vec3 forearm = skeleton.getVector(SkeletonJoint.RIGHT_ELBOW, SkeletonJoint.RIGHT_HAND);
    if (forearm == null) return;
    Vec3 forearmProjected = Maths.project(forearm, f, u);
    Vec3 forearmNormal = bicep.cross(forearm).normalize();

    if (bicep.dot(r) < 0) bicepNormal.mul(-1);
    float straightness = bicep.normalize().dot(forearm.normalize());
    float damping = (straightness > 0.9f) ? (straightness - 0.9f) / 0.1f : 0;
    if (forearmProjected.dot(bicepProjected) < 0 && bicepProjected.dot(f) < 0) {
      bicepNormal.mul(-1);
      bicepProjected.mul(-1);
    }

    set(nao.rShoulderPitch, Maths.angle(bicepProjected, f, r));
    set(nao.rShoulderRoll, Maths.angle(bicepProjected, bicep, bicepNormal));
    set(nao.rElbowRoll, Maths.angle(bicep, forearm, forearmNormal), damping);
    set(nao.rElbowYaw, Maths.angle(bicepNormal, forearmNormal, bicep), damping);
  }

  private void calcLeftArm(NiSkeleton skeleton, ReadableVec3 f, ReadableVec3 u, ReadableVec3 r) {
    if (f == null || u == null || r == null) return;
    
    Vec3 bicep = skeleton.getVector(SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_ELBOW);
    if (bicep == null) return;
    Vec3 bicepProjected = Maths.project(bicep, f, u);
    Vec3 bicepNormal = bicepProjected.cross(bicep).normalize();

    Vec3 forearm = skeleton.getVector(SkeletonJoint.LEFT_ELBOW, SkeletonJoint.LEFT_HAND);
    if (forearm == null) return;
    Vec3 forearmProjected = Maths.project(forearm, f, u);
    Vec3 forearmNormal = forearm.cross(bicep).normalize();
    
    if (bicep.dot(r) > 0) bicepNormal.mul(-1);
    float straightness = bicep.normalize().dot(forearm.normalize());
    float damping = (straightness > 0.9f) ? (straightness - 0.9f) / 0.1f : 0;
    if (forearmProjected.dot(bicepProjected) < 0 && bicepProjected.dot(f) < 0) {
      bicepNormal.mul(-1);
      bicepProjected.mul(-1);
    }
    
    set(nao.lShoulderPitch, Maths.angle(bicepProjected, f, r));
    set(nao.lShoulderRoll, Maths.angle(bicepProjected, bicep, bicepNormal));
    set(nao.lElbowRoll, Maths.angle(bicep, forearm, forearmNormal),damping);
    set(nao.lElbowYaw, Maths.angle(bicepNormal, forearmNormal, bicep), damping);
  }

  private void calcRightLeg(NiSkeleton skeleton, ReadableVec3 f, ReadableVec3 u, ReadableVec3 r) {
    if (f == null || u == null || r == null) return;

    ReadableVec3 thigh = skeleton.getVector(SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE);
    ReadableVec3 tibia = skeleton.getVector(SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT);
    if (thigh == null || tibia == null) return;
    thigh = thigh.normalize();
    tibia = tibia.normalize();

    // The joint positions from OpenNI have a bad habit of indicating the foot is ahead of the
    // knee; in other words, the leg is bent backwards. This is physically impossible for humans,
    // so if the knee ever appears like this the normal is flipped.
    Vec3 legNormal = skeleton.getJoint(SkeletonJoint.RIGHT_KNEE).getOrientation().getRight();
    if (legNormal.dot(r) < 0) legNormal.mul(-1);
    
    // A perfectly straight leg makes it impossible to determine the forward and normal vectors.
    // Since these are used to calculate the angles, this is the most problematic case. To
    // mitigate severe snapping, the legNormal is increasingly limited in its change from the
    // last known orientation as the straightness is greater.
    float straightness = thigh.dot(tibia);
    float damping = (straightness > 0.95f) ? (straightness - 0.95f) / 0.05f : 0.0f;
    if (prevLLegNormal != null && straightness > 0.9f) {
      Vec3 delta = legNormal.minus(prevRLegNormal);
      delta.mul(0.05f + 0.05f * (straightness - 0.9f) / 0.1f);
      legNormal = legNormal.plus(delta);
    }
    prevRLegNormal = legNormal;

    // The KneePitch is simply the angle between the thigh and tibia vectors. To avoid
    // excessive snapping when the leg is straight (the cross product flips), damping is used.
    set(nao.rKneePitch, Maths.angle(tibia, thigh, legNormal), damping);

    // To get the remaining angles, a pre-computed lookup table is used. This table lists all
    // pairs of HipYawPitch and HipRoll angles according to the possible configurations of the
    // leg's normal vector: the three joints (hip, knee, foot) create a plane with a unique
    // plane with the leg normal pointing out to the right. To get the angles, we calculate the
    // current 'legNormal' with respect to the lower torso in robot model space.
    legNormal = Maths.project(legNormal, r, u, f.times(-1)).normalize();
    legNormal = skeletonToModelSpace.transform(legNormal).normalize();

    HipConfig hipConfig = hipLookup.getBestConfiguration(legNormal, false);

    if (hipConfig != null) {
      // The HipYawPitch and HipRoll angles are pulled directly from the pre-computed table.
      set(nao.rHipYawPitch, (float) Math.toRadians(hipConfig.getYawPitch()));
      set(nao.rHipRoll, (float) Math.toRadians(hipConfig.getRoll()));

      // The HipPitch is the angle between the skeleton thigh vector and the computed thigh
      // vector stored in the hip table. Both legNormal and hipConfig.thigh are in robot
      // model space; the thigh vector is in skeleton space; the thigh vector must be
      // transformed from skeleton space -> skeleton torso space -> model space.
      Vec3 thighMS = skeletonToModelSpace.transform(Maths.project(thigh, r, u, f.times(-1)));
      set(nao.rHipPitch, Maths.angle(thighMS, hipConfig.getThigh(), legNormal));
    }
  }

  private void calcLeftLeg(NiSkeleton skeleton, ReadableVec3 f, ReadableVec3 u, ReadableVec3 r) {
    if (f == null || u == null || r == null) return;

    ReadableVec3 thigh = skeleton.getVector(SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE);
    ReadableVec3 tibia = skeleton.getVector(SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT);
    if (thigh == null || tibia == null) return;
    thigh = thigh.normalize();
    tibia = tibia.normalize();

    Vec3 legNormal = skeleton.getJoint(SkeletonJoint.LEFT_KNEE).getOrientation().getRight();
    if (legNormal.dot(r) < 0)  legNormal.mul(-1); 
    
    float straightness = thigh.dot(tibia);
    float damping = (straightness > 0.95f) ? (straightness - 0.95f) / 0.05f : 0.0f;
    if (prevLLegNormal != null && straightness > 0.9f) {
      Vec3 delta = legNormal.minus(prevLLegNormal);
      delta.mul(0.05f + 0.05f * (straightness - 0.9f) / 0.1f);
      legNormal = legNormal.plus(delta);
    }
    prevLLegNormal = legNormal;

    set(nao.lKneePitch, Maths.angle(tibia, thigh, legNormal), damping);

    legNormal = Maths.project(legNormal, r, u, f.times(-1)).normalize();
    legNormal = skeletonToModelSpace.transform(legNormal).normalize();

    HipConfig hipConfig = hipLookup.getBestConfiguration(legNormal, true);
    if (hipConfig != null) {
      set(nao.lHipYawPitch, (float) Math.toRadians(hipConfig.getYawPitch()));
      set(nao.lHipRoll, (float) Math.toRadians(hipConfig.getRoll()));

      Vec3 thighMS = skeletonToModelSpace.transform(Maths.project(thigh, r, u, f.times(-1)));
      set(nao.lHipPitch, Maths.angle(thighMS, hipConfig.getThigh(), legNormal));
    }
  }

  private void set(RevoluteJoint joint, float radians, float damping) {
     radians = (float) filters.get(joint).filter(radians);
     float curRadians = joint.getRadians();
     float deltaRadians = (radians - curRadians) * (1 - damping);
     deltaRadians = GMath.clamp(deltaRadians, -.1f, .1f);
     radians = curRadians + deltaRadians;
    
    joint.setRadians(radians);
  }

  private void set(RevoluteJoint joint, float radians) {
    set(joint, radians, 0);
  }
  
  private void set(RevoluteJoint joint, float radians, float damping, float maxChange) {
    if (Math.abs(joint.getRadians() - radians) > maxChange) return;
    // shouldnt max change really be max change within a time frame? 
    set(joint, radians, damping);
  }
}
