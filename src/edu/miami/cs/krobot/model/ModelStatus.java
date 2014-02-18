package edu.miami.cs.krobot.model;

import java.util.HashMap;
import edu.miami.cs.js.math.vector.Orientation;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.model.joint.Joint1DOF;
import edu.miami.cs.krobot.model.joint.RevoluteJoint;

/**
 * Contains an overview of the model's current configuration as a readable message. The format of
 * this status is set by the model configuration file.
 * 
 * @author justin
 */
public class ModelStatus {

  public Object[]                elements;
  private HashMap<String, Object> properties = new HashMap<String, Object>();
  public boolean                 radians;

  public ModelStatus() {
  }
  
  public ModelStatus(Object[] elements, boolean radians) {
    this.elements = elements;
    this.radians = radians;
  }

  public HashMap<String, Object> getProperties() {
    return properties;
  }

  private String jointStr(Joint1DOF j) {
    if (radians && j instanceof RevoluteJoint) {
      return Float.toString(((RevoluteJoint) j).getRadians());
    }
    return Float.toString(j.getValue());
  }

  private String vec3Str(Vec3 v) {
    return (v == null) ? "? ? ?" : String.format("%f %f %f", v.x, v.y, v.z);
  }

  private String orientationStr(Orientation o) {
    String fs = vec3Str(o.getForward());
    String us = vec3Str(o.getUp());
    String rs = vec3Str(o.getRight());
    return String.format("%s %s %s", fs, us, rs);
  }

  private String chainStr(KinematicChain c) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < c.parts.size(); i++) {
      if (c.parts.get(i) instanceof Joint1DOF) {
        builder.append(jointStr((Joint1DOF) c.parts.get(i)));
        builder.append(" ");
      }
    }
    return builder.toString().trim();
  }

  private String objectStr(Object o) {
    if (o instanceof Joint1DOF) return jointStr((Joint1DOF) o);
    if (o instanceof Orientation) return orientationStr((Orientation) o);
    if (o instanceof Vec3) return vec3Str((Vec3) o);
    if (o instanceof KinematicChain) return chainStr((KinematicChain) o);
    return o.toString();
  }

  private String elementStr(Object element) {
    if (element == null) return "null";

    if (element instanceof String) {
      Object propertyValue = properties.get(element);
      return (propertyValue == null) ? element.toString() : objectStr(propertyValue);
    }

    return objectStr(element);
  }

  @Override
  public String toString() {

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < elements.length; i++) {
      builder.append(elementStr(elements[i]));
      if (i != elements.length - 1) builder.append(" ");
    }
    return builder.toString();
  }
}
