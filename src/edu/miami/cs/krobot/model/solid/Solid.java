package edu.miami.cs.krobot.model.solid;

import edu.miami.cs.js.math.vector.Mat4;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.model.ModelPart;

/**
 * Solids are rigid bodies that possess mass. They can be described by their position and
 * orientation in space. Solids do not transform other solids they are connected to. Examples on a
 * humanoid include a torso, head, hand, foot, etc. 
 * 
 * @author justin
 */
public abstract class Solid extends ModelPart {

  protected float mass;
  
  public Solid() {
  }
  
  public Solid(String name, ModelPart parent, Vec3 offset, float mass) {
    super(name, parent, offset);
    this.mass = mass;
  }
  
  public float getMass() {
    return mass;
  }
  
  public void setMass(float mass) {
    this.mass = mass;
  }
  
  @Override
  protected void updateLocalTransform() {
    localTransform = Mat4.createTranslation(offset);
  }
}
