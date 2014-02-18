package edu.miami.cs.krobot.openni;

import org.OpenNI.SkeletonJoint;
import edu.miami.cs.js.math.vector.Orientation;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.Config;
import edu.miami.cs.krobot.util.IIRFilter;

/**
 * OpenNI user skeleton joint that contains a position and orientation in sensor space. If filtering
 * is active in the configuration, it also provides a smoothed position.
 * 
 * @author justin
 */
public class NiJoint {

  final SkeletonJoint id;
  Vec3                position;
  Vec3                positionRaw;
  boolean             filtering;
  IIRFilter           pxFilter;
  IIRFilter           pyFilter;
  IIRFilter           pzFilter;
  Orientation         orientation;

  protected NiJoint(SkeletonJoint jointID, Config config) {
    this.id = jointID;

    if (filtering = config.filterSkeleton) {
      pxFilter = new IIRFilter(config.filterSkelCoeffB, config.filterSkelCoeffA);
      pyFilter = new IIRFilter(config.filterSkelCoeffB, config.filterSkelCoeffA);
      pzFilter = new IIRFilter(config.filterSkelCoeffB, config.filterSkelCoeffA);
    }
  }

  /** Orientation matrix for the joint provided by OpenNI */
  public Orientation getOrientation() {
    return orientation;
  }

  /** ID of the joint from the OpenNI SkeletonJoint enum */
  public SkeletonJoint getJointID() {
    return id;
  }

  /** Returns the unfiltered position in sensor space */
  public Vec3 getPositionRaw() {
    return positionRaw;
  }

  /** Returns the (possibly) filtered position in sensor space */
  public Vec3 getPosition() {
    return position;
  }
  
  /**
   * Sets the most recent unfiltered position of the joint. If filtering is enabled, the filtered
   * position is computed automatically.
   */
  protected void setPositionRaw(Vec3 p) {
    if (p == null) {
      positionRaw = position = null;
      return;
    }
    
    positionRaw = p.clone();
    if (filtering) {
      float x = (float) pxFilter.filter(positionRaw.x);
      float y = (float) pyFilter.filter(positionRaw.y);
      float z = (float) pzFilter.filter(positionRaw.z);
      position = new Vec3(x, y, z);
    } else {
      position = positionRaw;
    }
  }
}
