package edu.miami.cs.krobot.model;

import java.util.HashMap;
import java.util.Map.Entry;
import edu.miami.cs.krobot.model.joint.Joint1DOF;

/**
 * Contains a mapping of joint names to values.
 * 
 * @author justin
 */
public class ModelPose {

  public String                    name;
  public HashMap<Joint1DOF, Number> values;

  @Override
  public String toString() {
    return name;
  }

  public void apply(Model model) {
    for (Entry<Joint1DOF, Number> mapping : values.entrySet()) {
      mapping.getKey().setValue(mapping.getValue().floatValue());
    }
  }
}
