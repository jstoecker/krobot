package edu.miami.cs.krobot.util;

import edu.miami.cs.js.math.vector.ReadableVec3;
import edu.miami.cs.js.math.vector.Vec3;

public class Maths {

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
  public static float angle(ReadableVec3 a, ReadableVec3 b, ReadableVec3 u) {
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
  public static Vec3 project(ReadableVec3 v, ReadableVec3 x, ReadableVec3 y) {
    return x.times(x.dot(v)).plus(y.times(y.dot(v)));
  }
  
  /**
   * Projects a vector onto the basis vectors of a coordinate system.
   */
  public static Vec3 project(ReadableVec3 v, ReadableVec3 x, ReadableVec3 y, ReadableVec3 z) {
    return new Vec3(x.dot(v), y.dot(v), z.dot(v));
  }

  /** Mirror vector v across plane with normal n */
  static Vec3 mirror(ReadableVec3 v, ReadableVec3 n) {
    return v.minus(n.times(2 * v.dot(n)));
  }
}
