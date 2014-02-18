package edu.miami.cs.krobot.model.solid;

import javax.media.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.ModelPart;

public class Box extends Solid {

  static int boxList = -1;

  private float width, height, length;
  
  public Box() {
  }

  public Box(String name, ModelPart parent, Vec3 offset, float width, float length, float height, float mass) {
    super(name, parent, offset, mass);
    this.width = width;
    this.length = length;
    this.height = height;
  }

  public void draw(GL2 gl) {
    gl.glPushMatrix();
    gl.glScaled(width, length, height);
    gl.glCallList(boxList);
    gl.glPopMatrix();
  }

  public static void init(GL2 gl) {
    boxList = gl.glGenLists(1);
    gl.glNewList(boxList, GL2.GL_COMPILE);
    new GLUT().glutSolidCube(1);
    gl.glEndList();
  }

  public static void dispose(GL2 gl) {
    gl.glDeleteLists(1, boxList);
  }
  
  public void setWidth(float width) {
    this.width = width;
  }
  
  public void setHeight(float height) {
    this.height = height;
  }
  
  public void setLength(float length) {
    this.length = length;
  }

  @Override
  protected Box clone(Model clone) {
    return new Box(name, parent == null ? clone.getRoot() : clone.getPart(parent.getName()),
        offset.clone(), width, length, height, mass);
  }
}
