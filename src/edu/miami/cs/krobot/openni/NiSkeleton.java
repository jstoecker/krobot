package edu.miami.cs.krobot.openni;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.OpenNI.SkeletonJoint;
import edu.miami.cs.js.math.vector.Orientation;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.Config;

/**
 * Contains skeleton tracking joint data for a single OpenNI user.
 * 
 * @author justin
 */
public class NiSkeleton {

  final int                         id;
  final List<NiJoint>               joints   = new ArrayList<NiJoint>();
  final Map<SkeletonJoint, NiJoint> jointMap = new EnumMap<SkeletonJoint, NiJoint>(
                                                 SkeletonJoint.class);
  Orientation                       orientationUpper;
  Orientation                       orientationLower;

  protected NiSkeleton(int userId, List<SkeletonJoint> jointIds, Config config) {
    this.id = userId;
    
    for (SkeletonJoint jointId : jointIds) {
      NiJoint joint = new NiJoint(jointId, config);
      joints.add(joint);
      jointMap.put(jointId, joint);
    }
  }

  public List<NiJoint> getJoints() {
    return Collections.unmodifiableList(joints);
  }

  public NiJoint getJoint(SkeletonJoint jointID) {
    return jointMap.get(jointID);
  }

  public Orientation getUpperOrientation() {
    return orientationUpper;
  }

  public Orientation getLowerOrientation() {
    return orientationLower;
  }

  protected void updateOrientation() {
    calcLowerOrientation();
    calcUpperOrientation();
  }

  private void calcLowerOrientation() {
    if (!hasValidJoints(new SkeletonJoint[] { SkeletonJoint.TORSO, SkeletonJoint.RIGHT_HIP,
        SkeletonJoint.LEFT_HIP })) {
      orientationLower = new Orientation(null, null, null);
      return;
    }

    Vec3 a = getVector(SkeletonJoint.TORSO, SkeletonJoint.RIGHT_HIP);
    Vec3 b = getVector(SkeletonJoint.TORSO, SkeletonJoint.LEFT_HIP);
    Vec3 f = a.cross(b).normalize();
    Vec3 r = getVector(SkeletonJoint.LEFT_HIP, SkeletonJoint.RIGHT_HIP).normalize();
    Vec3 u = r.cross(f).normalize();
    orientationLower = new Orientation(f, u, r);
  }

  private void calcUpperOrientation() {
    if (!hasValidJoints(new SkeletonJoint[] { SkeletonJoint.TORSO, SkeletonJoint.RIGHT_SHOULDER,
        SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.NECK })) {
      orientationUpper = new Orientation(null, null, null);
      return;
    }

    Vec3 a = getVector(SkeletonJoint.TORSO, SkeletonJoint.LEFT_SHOULDER);
    Vec3 b = getVector(SkeletonJoint.TORSO, SkeletonJoint.RIGHT_SHOULDER);
    Vec3 f = a.cross(b).normalize();
    Vec3 r = getVector(SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.RIGHT_SHOULDER).normalize();
    Vec3 u = r.cross(f).normalize();
    orientationUpper = new Orientation(f, u, r);
  }

  private boolean hasValidJoints(SkeletonJoint[] required) {
    for (int i = 0; i < required.length; i++)
      if (getJoint(required[i]).getPosition() == null) return false;
    return true;
  }

  /** Returns a vector from the joint SRC to the joint TGT */
  public Vec3 getVector(SkeletonJoint src, SkeletonJoint tgt) {
    Vec3 ap = getJoint(src).getPosition();
    Vec3 bp = getJoint(tgt).getPosition();
    return (ap != null && bp != null) ? bp.minus(ap) : null;
  }
}
