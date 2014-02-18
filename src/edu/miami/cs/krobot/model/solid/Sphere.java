package edu.miami.cs.krobot.model.solid;

import javax.media.opengl.GL2;
import edu.miami.cs.js.jgloo.draw.GLGeosphere;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.ModelPart;

public class Sphere extends Solid {

  private static int sphList = -1;
  private float      radius;
  
  public Sphere() {
  }

  public Sphere(String name, ModelPart parent, Vec3 offset, float radius, float mass) {
    super(name, parent, offset, mass);
    this.radius = radius;
  }
  
  public void setRadius(float radius) {
    this.radius = radius;
  }

  public void draw(GL2 gl) {
    gl.glPushMatrix();
    gl.glScaled(radius, radius, radius);
    gl.glCallList(sphList);
    gl.glPopMatrix();
  }

  public static void init(GL2 gl) {
    sphList = gl.glGenLists(1);
    gl.glNewList(sphList, GL2.GL_COMPILE);
    new GLGeosphere(1, 2).render(gl);
    gl.glEndList();
  }

  public static void dispose(GL2 gl) {
    gl.glDeleteLists(1, sphList);
  }

  @Override
  protected Sphere clone(Model clone) {
    return new Sphere(name, parent == null ? clone.getRoot() : clone.getPart(parent.getName()),
        offset.clone(), radius, mass);
  }
}
