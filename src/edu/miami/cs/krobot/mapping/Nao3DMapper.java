package edu.miami.cs.krobot.mapping;

import java.util.HashMap;
import org.OpenNI.SkeletonJoint;
import edu.miami.cs.js.math.util.GMath;
import edu.miami.cs.js.math.vector.Mat4;
import edu.miami.cs.js.math.vector.Orientation;
import edu.miami.cs.js.math.vector.ReadableVec3;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.mapping.Nao3DHipTable.HipConfig;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.joint.Joint1DOF;
import edu.miami.cs.krobot.model.joint.RevoluteJoint;
import edu.miami.cs.krobot.openni.NiSkeleton;
import edu.miami.cs.krobot.util.IIRFilter;
import edu.miami.cs.krobot.util.Maths;

public class Nao3DMapper extends NiSkeletonMapper {

  // coefficients for a 2nd order Butterworth low-pass filter with cutoff of 1/10 the sampling rate
  static final double[]         BUTTER_B             = { 0.0675, 0.1349, 0.0675 };
  static final double[]         BUTTER_A             = { 1.0000, -1.1430, 0.4128 };

  private Nao3D                 nao;
  HashMap<Joint1DOF, IIRFilter> filters              = new HashMap<Joint1DOF, IIRFilter>();
  Nao3DHipTable                 hipLookup;

  Mat4                          skeletonToModelSpace = Mat4.createRotationX(Math.PI / 2);

  @Override
  public void initialize(Model model) {
    super.initialize(model);
    nao = new Nao3D(model);
    hipLookup = Nao3DHipTable.build(nao, 1);
    
    model.getStatus().getProperties().put("orientation_up", model.up);
    model.getStatus().getProperties().put("orientation_fwd", model.forward);

    // reset joints after building hip table
    for (Joint1DOF joint : model.getJoints())
      joint.setValue(0);

    filters.clear();
    for (Joint1DOF joint : model.getJoints())
      filters.put(joint, new IIRFilter(BUTTER_B, BUTTER_A));
  }

  /**
   * Computes the angle that rotates vector A into B around an axis U using the right-hand rule.
   * Returns an angle in [-pi,pi]. For example, if a=(1,0,0), b=(0,1,0), u=(0,0,1): angle(a,b,u) =
   * pi/2; angle(b,a,u) = -pi/2.
   * 
   * @param a - first vector
   * @param b - second vector
   * @param u - rotation axis (should be perpendicular to both a and b)
   * @return - angle in radians in [-pi,pi]
   */
  private static float angle(ReadableVec3 a, ReadableVec3 b, ReadableVec3 u) {
    Vec3 c = a.cross(b);
    double y = c.length();
    double x = a.dot(b);
    double rads = Math.atan2(y, x);
    if (c.dot(u) < 0) rads *= -1;
    return (float) rads;
  }

  /**
   * Projects a vector V onto the plane spanned by X and Y.
   * 
   * @param v - vector to project
   * @param x - basis vector 1
   * @param y - basis vector 2
   * @return a vector on the plane spanned by X and Y
   */
  private static Vec3 project(ReadableVec3 v, ReadableVec3 x, ReadableVec3 y) {
    return x.times(x.dot(v)).plus(y.times(y.dot(v)));
  }

  private static Vec3 project(ReadableVec3 v, ReadableVec3 x, ReadableVec3 y, ReadableVec3 z) {
    return x.times(x.dot(v)).plus(y.times(y.dot(v))).plus(z.times(z.dot(v)));
  }

  /** mirror vector v across plane with normal n */
  static Vec3 mirror(ReadableVec3 v, ReadableVec3 n) {
    return v.minus(n.times(2 * v.dot(n)));
  }

  private void setRevJointAngle(RevoluteJoint joint, float radians) {
    IIRFilter butterworth = filters.get(joint);

    // low-pass filter
    radians = (float) butterworth.filter(radians);

    // limit max. change in angle
    float curRadians = joint.getRadians();
    float deltaRadians = radians - curRadians;
    deltaRadians = GMath.clamp(deltaRadians, -.1f, .1f);
    radians = curRadians + deltaRadians;

    // set joint angle
    joint.setRadians(radians);
  }

  @Override
  public void map(NiSkeleton skeleton) {

    Orientation o = skeleton.getUpperOrientation();
    if (o == null) return;
    ReadableVec3 f = o.getForward();
    ReadableVec3 r = o.getRight();
    ReadableVec3 u = o.getUp();

    mapRightArm(skeleton, f, u, r);
    mapLeftArm(skeleton, f, u, r);

    mapOrientation(o);

    o = skeleton.getLowerOrientation();
    if (o == null) return;
    f = o.getForward();
    r = o.getRight();
    u = o.getUp();

    calcRightLeg(skeleton, f, u, r);
    calcLeftLeg(skeleton, f, u, r);

    // TODO: determine support foot by the leg with lowest height; assume foot must be planar with
    // ground, so the angle between the leg and the ground plane is the foot roll.

    // Vec3 lfp = model.getPart("Left Foot").getPosition();
    // Vec3 rfp = model.getPart("Right Foot").getPosition();
    // if (lfp != null && rfp != null) {
    //
    // if (Math.abs(lfp.z - rfp.z) < 0.02) {
    // // both support
    // // model.setModelMatrix(Mat4.createIdentity());
    // } else if (lfp.z < rfp.z) {
    // // left support
    // // model.setModelMatrix(Mat4.createRotationY(-nao.lHipRoll.getRadians()));
    // } else {
    // // right support
    // // model.setModelMatrix(Mat4.createRotationY(-Math.PI / 3));
    // }
    // }
  }
  
  private void mapOrientation(Orientation o) {
    if (o.getForward() != null && o.getUp() != null && o.getRight() != null) {
      Vec3 mf = skeletonToModelSpace.transform(o.getForward());
      Vec3 mu = skeletonToModelSpace.transform(o.getUp());
      nao.model.getStatus().getProperties().put("orientation_up", mu);
      nao.model.getStatus().getProperties().put("orientation_fwd", mf);
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

  private void mapRightArm(NiSkeleton skeleton, ReadableVec3 f, ReadableVec3 u, ReadableVec3 r) {
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
    
    set(nao.rShoulderPitch, -Maths.angle(bicepProjected, f, r));
    set(nao.rShoulderYaw, Maths.angle(bicepProjected, bicep, bicepNormal));
    set(nao.rArmYaw, Maths.angle(bicep, forearm, forearmNormal));
    set(nao.rArmRoll, Maths.angle(bicepNormal, forearmNormal, bicep), damping);
  }

  private void mapLeftArm(NiSkeleton skeleton, ReadableVec3 f, ReadableVec3 u, ReadableVec3 r) {
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
    
    set(nao.lShoulderPitch, -Maths.angle(bicepProjected, f, r));
    set(nao.lShoulderYaw, Maths.angle(bicepProjected, bicep, bicepNormal));
    set(nao.lArmYaw, Maths.angle(bicep, forearm, forearmNormal));
    set(nao.lArmRoll, Maths.angle(bicepNormal, forearmNormal, bicep), damping);
  }

  private void calcRightLeg(NiSkeleton skeleton, ReadableVec3 f, ReadableVec3 u, ReadableVec3 r) {
    ReadableVec3 thigh = skeleton.getVector(SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE);
    ReadableVec3 tibia = skeleton.getVector(SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT);
    if (thigh == null || tibia == null) return;
    thigh = thigh.normalize();
    tibia = tibia.normalize();
    
    Vec3 legNormal = skeleton.getJoint(SkeletonJoint.RIGHT_KNEE).getOrientation().getRight();
    if (legNormal.dot(r) < 0) legNormal.mul(-1);

    float straightness = thigh.dot(tibia);
    float damping = (straightness > 0.95f) ? (straightness - 0.95f) / 0.05f : 0.0f;

    set(nao.rKneePitch, -Maths.angle(tibia, thigh, legNormal), damping);

    legNormal = Maths.project(legNormal, r, u, f.times(-1)).normalize();
    legNormal = skeletonToModelSpace.transform(legNormal).normalize();

    HipConfig hipConfig = hipLookup.getBestConfiguration(legNormal, false);

    if (hipConfig != null) {
      set(nao.rHipYawPitch, (float) Math.toRadians(hipConfig.getYawPitch()));
      set(nao.rHipRoll, (float) Math.toRadians(hipConfig.getRoll()));

      Vec3 thighMS = skeletonToModelSpace.transform(Maths.project(thigh, r, u, f.times(-1)));
      set(nao.rHipPitch, -Maths.angle(thighMS, hipConfig.getThigh(), legNormal));
    }
  }

  private void calcLeftLeg(NiSkeleton skeleton, ReadableVec3 f, ReadableVec3 u, ReadableVec3 r) {
    Vec3 thigh = skeleton.getVector(SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE);
    Vec3 tibia = skeleton.getVector(SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT);
    if (thigh == null || tibia == null) return;
    thigh = thigh.normalize();
    tibia = tibia.normalize();

    Vec3 legNormal = skeleton.getJoint(SkeletonJoint.LEFT_KNEE).getOrientation().getRight();
    if (legNormal.dot(r) < 0)  legNormal.mul(-1);
    
    float straightness = thigh.dot(tibia);
    float damping = (straightness > 0.95f) ? (straightness - 0.95f) / 0.05f : 0.0f;
    
    set(nao.lKneePitch, -Maths.angle(tibia, thigh, legNormal), damping);
    
    legNormal = Maths.project(legNormal, r, u, f.times(-1)).normalize();
    legNormal = skeletonToModelSpace.transform(legNormal).normalize();
    
    HipConfig hipConfig = hipLookup.getBestConfiguration(legNormal, true);
    if (hipConfig != null) {
      set(nao.lHipYawPitch, (float) Math.toRadians(hipConfig.getYawPitch()));
      set(nao.lHipRoll, (float) Math.toRadians(hipConfig.getRoll()));

      Vec3 thighMS = skeletonToModelSpace.transform(Maths.project(thigh, r, u, f.times(-1)));
      set(nao.lHipPitch, -Maths.angle(thighMS, hipConfig.getThigh(), legNormal));
    }
  }
}
