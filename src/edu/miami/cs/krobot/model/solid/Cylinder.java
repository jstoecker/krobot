package edu.miami.cs.krobot.model.solid;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import edu.miami.cs.js.math.util.GMath;
import edu.miami.cs.js.math.vector.Mat4;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.ModelPart;

public class Cylinder extends Solid {

  private static int cylList = -1;

  private float      length, radius;
  private Mat4       modelMat;
  private Vec3       axis;
  
  public Cylinder() {
  }

  public Cylinder(String name, ModelPart parent, Vec3 offset, Vec3 axis, float length,
      float radius, float mass) {
    super(name, parent, offset, mass);
    this.length = length;
    this.radius = radius;
    this.axis = axis.normalize();
    updateModelMat();
  }

  private void updateModelMat() {
    Mat4 scale = Mat4.createScale(new Vec3(radius, radius, length));
    Vec3 z = new Vec3(0, 0, 1);
    if (z.dot(axis) > 0.999) {
      modelMat = scale;
    } else {
      double angle = GMath.vecAngle(axis, z);
      Mat4 rotation = Mat4.createRotation(angle, z.cross(axis));
      modelMat = rotation.times(scale);
    }
  }

  public void draw(GL2 gl) {
    if (modelMat == null) updateModelMat();
    gl.glPushMatrix();
    gl.glMultMatrixf(modelMat.values(), 0);
    gl.glCallList(cylList);
    gl.glPopMatrix();
  }

  public static void init(GL2 gl) {
    cylList = gl.glGenLists(1);
    GLU glu = new GLU();
    GLUquadric quadric = glu.gluNewQuadric();
    gl.glNewList(cylList, GL2.GL_COMPILE);
    gl.glPushMatrix();
    gl.glTranslated(0, 0, -0.5);
    glu.gluCylinder(quadric, 1, 1, 1, 8, 8);
    gl.glPopMatrix();
    gl.glEndList();
  }
  
  public void setLength(float length) {
    this.length = length;
  }
  
  public void setRadius(float radius) {
    this.radius = radius;
  }
  
  public void setAxis(Vec3 axis) {
    this.axis = axis;
  }
  
  public static void dispose(GL2 gl) {
    gl.glDeleteLists(1, cylList);
  }

  @Override
  protected Cylinder clone(Model clone) {
    return new Cylinder(name, parent == null ? clone.getRoot() : clone.getPart(parent.getName()),
        offset.clone(), axis.clone(), length, radius, mass);
  }
}
