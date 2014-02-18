package edu.miami.cs.krobot.model;

import javax.media.opengl.GL2;
import edu.miami.cs.js.math.vector.Mat4;
import edu.miami.cs.js.math.vector.Vec3;

public class RootPart extends ModelPart {

  public RootPart(String name, ModelPart parent, Vec3 offset) {
    super(name, parent, offset);
    updateLocalTransform();
  }

  @Override
  public void draw(GL2 gl) {
  }

  @Override
  protected ModelPart clone(Model clone) {
    return new RootPart(name, parent == null ? clone.getRoot() : clone.getPart(parent.name), offset.clone());
  }

  @Override
  protected void updateLocalTransform() {
    localTransform = Mat4.createTranslation(offset);
  }
}
