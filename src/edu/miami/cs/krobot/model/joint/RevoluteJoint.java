package edu.miami.cs.krobot.model.joint;

import javax.media.opengl.GL2;
import edu.miami.cs.js.math.vector.Mat4;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.ModelPart;

/**
 * Single degree of freedom joint that provides rotation around on axis. By default, the current
 * value is always expressed in degrees.
 * 
 * @author justin
 */
public class RevoluteJoint extends Joint1DOF {

  public RevoluteJoint() {
  }

  public RevoluteJoint(String name, ModelPart parent, Vec3 offset, Vec3 axis, float minDegrees, float maxDegrees) {
    super(name, parent, offset, axis, minDegrees, maxDegrees);
  }
  
  public float getRadians() {
    return (float)Math.toRadians(value);
  }
  
  public void setDegrees(float degrees) {
    setValue(degrees);
  }
  
  public void setRadians(float radians) {
    setValue((float)Math.toDegrees(radians));
  }

  @Override
  protected void updateLocalTransform() {
    Mat4 t = Mat4.createTranslation(offset);
    Mat4 r = Mat4.createRotation(getRadians(), axis);
    localTransform = t.times(r);
  }

  @Override
  public void draw(GL2 gl) {
    gl.glBegin(GL2.GL_LINES);
    gl.glVertex3d(-.05, 0, 0);
    gl.glVertex3d(.05, 0, 0);
    gl.glVertex3d(0, -.05, 0);
    gl.glVertex3d(0, .05, 0);
    gl.glVertex3d(0, 0, -.05);
    gl.glVertex3d(0, 0, .05);
    gl.glEnd();   
  }

  @Override
  protected RevoluteJoint clone(Model clone) {
    return new RevoluteJoint(name, parent == null ? clone.getRoot() : clone.getPart(parent.getName()), offset.clone(), axis.clone(), minValue, maxValue);
  }
  
  @Override
  public String toString() {
    return Float.toString(value);
  }
}
