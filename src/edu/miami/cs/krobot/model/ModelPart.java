package edu.miami.cs.krobot.model;

import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL2;
import edu.miami.cs.js.math.vector.Mat4;
import edu.miami.cs.js.math.vector.Vec3;

/**
 * A joint or a solid.
 * 
 * @author justin
 */
public abstract class ModelPart {

  protected String               name;
  protected ModelPart            parent          = null;
  protected Vec3                 offset          = new Vec3(0);
  protected Vec3                 position        = new Vec3(0);
  protected ArrayList<ModelPart> children        = new ArrayList<ModelPart>();
  protected Mat4                 globalTransform = Mat4.createIdentity();
  protected Mat4                 localTransform  = Mat4.createIdentity();
  protected KinematicChain       chain;

  public ModelPart() {
  }

  public ModelPart(String name, ModelPart parent, Vec3 offset) {
    this.name = name;
    this.parent = parent;
    this.offset = offset;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public KinematicChain getChain() {
    return chain;
  }

  public void setOffset(Vec3 offset) {
    this.offset = offset;
    updateLocalTransform();
  }

  public void setParent(ModelPart parent) {
    this.parent = parent;
    updateLocalTransform();
  }

  public String getName() {
    return name;
  }

  public Vec3 getPosition() {
    return position;
  }

  public Mat4 getLocalTransform() {
    return localTransform;
  }

  public Mat4 getGlobalTransform() {
    return globalTransform;
  }

  public List<ModelPart> getChildren() {
    return children;
  }

  protected abstract void updateLocalTransform();

  protected void update() {
    globalTransform = (parent == null) ? localTransform : parent.getGlobalTransform().times(
        localTransform);
    position.x = globalTransform.get(0, 3);
    position.y = globalTransform.get(1, 3);
    position.z = globalTransform.get(2, 3);

    for (ModelPart child : children)
      child.update();
  }

  public abstract void draw(GL2 gl);

  protected abstract ModelPart clone(Model clone);
}
