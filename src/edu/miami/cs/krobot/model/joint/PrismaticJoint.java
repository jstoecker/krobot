package edu.miami.cs.krobot.model.joint;

import javax.media.opengl.GL2;
import edu.miami.cs.js.math.vector.Mat4;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.ModelPart;

public class PrismaticJoint extends Joint1DOF {
  
  public PrismaticJoint() {
  }
  
  public PrismaticJoint(String name, ModelPart parent, Vec3 offset, Vec3 axis, float minDistance, float maxDistance) {
    super(name, parent, offset, axis, minDistance, maxDistance);
  }
  
  @Override
  protected void updateLocalTransform() {
    localTransform = Mat4.createTranslation(axis.times(value).plus(offset));
  }

  @Override
  public void draw(GL2 gl) {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected PrismaticJoint clone(Model clone) {
    return new PrismaticJoint(name, parent == null ? clone.getRoot() : clone.getPart(parent.getName()), offset.clone(), axis.clone(), minValue, maxValue);
  }
  
  @Override
  public String toString() {
    return Float.toString(value);
  }
}
