package edu.miami.cs.krobot.view;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import org.OpenNI.SkeletonJoint;
import edu.miami.cs.js.jgloo.draw.Draw;
import edu.miami.cs.js.jgloo.view.OrbitCamera;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.openni.NiJoint;
import edu.miami.cs.krobot.openni.NiSkeleton;
import edu.miami.cs.krobot.openni.NiSkeletonSrcListener;

public class SkeletonView extends GLView implements NiSkeletonSrcListener {

  GLU         glu    = new GLU();
  NiSkeleton  skeleton;
  OrbitCamera camera = new OrbitCamera(3, (float) Math.PI / 8, (float) Math.PI, new Vec3(0), true);
  boolean     raw    = true;

  public SkeletonView(String name, MasterView mainView) {
    super(name, mainView);
  }

  public OrbitCamera getCamera() {
    return camera;
  }

  @Override
  public void init(GL2 gl) {
  }

  @Override
  public void dispose(GL2 gl) {
  }

  @Override
  public void draw(GL2 gl) {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    if (skeleton == null) return;
    drawPerspectiveView(gl);
  }

  private void drawPerspectiveView(GL2 gl) {
    Vec3 avgPos = new Vec3(0);
    int pts = 0;
    for (NiJoint joint : skeleton.getJoints()) {
      if (joint.getPosition() != null) {
        avgPos.add(joint.getPosition());
        pts++;
      }
    }

    if (pts > 0) {
      avgPos.div(1000 * pts);
      camera.setViewport(viewport);
      // camera.setViewport(new Viewport(viewport.x + (viewport.w/3)*2, viewport.y, viewport.w / 3,
      // viewport.h));
      camera.setCenter(avgPos, false);
      camera.apply(gl);
    }

    gl.glColor3f(0.3f, 0.3f, 0.3f);
    gl.glLineWidth(2);
    Draw.grid(gl, 5, 0.2);

    Draw.axes(gl, 1);

    gl.glLineWidth(5);
    gl.glPointSize(16);
//    raw = true;
//    drawSkeleton(gl);
    raw = false;
    drawSkeleton(gl);
  }

  private void drawSkeleton(GL2 gl) {
    if (raw) gl.glColor3f(0.3f, 0.3f, 0.3f);
    gl.glBegin(GL2.GL_LINES);
    if (!raw) gl.glColor3f(1, 1, 0);
    drawPart(gl, SkeletonJoint.NECK, SkeletonJoint.HEAD);
    drawPart(gl, SkeletonJoint.RIGHT_HIP, SkeletonJoint.LEFT_HIP);
    if (!raw) gl.glColor3f(1, 0, 0);
    drawPart(gl, SkeletonJoint.TORSO, SkeletonJoint.RIGHT_SHOULDER);
    drawPart(gl, SkeletonJoint.TORSO, SkeletonJoint.RIGHT_HIP);
    drawPart(gl, SkeletonJoint.NECK, SkeletonJoint.RIGHT_SHOULDER);
    drawPart(gl, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.RIGHT_ELBOW);
    drawPart(gl, SkeletonJoint.RIGHT_ELBOW, SkeletonJoint.RIGHT_HAND);
    drawPart(gl, SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE);
    drawPart(gl, SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT);
    if (!raw) gl.glColor3f(0, 1, 0);
    drawPart(gl, SkeletonJoint.TORSO, SkeletonJoint.LEFT_SHOULDER);
    drawPart(gl, SkeletonJoint.TORSO, SkeletonJoint.LEFT_HIP);
    drawPart(gl, SkeletonJoint.NECK, SkeletonJoint.LEFT_SHOULDER);
    drawPart(gl, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_ELBOW);
    drawPart(gl, SkeletonJoint.LEFT_ELBOW, SkeletonJoint.LEFT_HAND);
    drawPart(gl, SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE);
    drawPart(gl, SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT);
    gl.glEnd();

    gl.glBegin(GL2.GL_POINTS);
    if (raw)
      gl.glColor3f(0.4f, 0.4f, 0.4f);
    else
      gl.glColor3f(1, 1, 1);
    for (NiJoint joint : skeleton.getJoints()) {
      if (joint.getPosition() != null) {
        Vec3 p = raw ? joint.getPositionRaw().over(1000) : joint.getPosition().over(1000);
        gl.glVertex3f(p.x, p.y, p.z);
      }
    }
    gl.glEnd();
  }

  private void drawQuad(GL2 gl, float x, float y, float w, float h) {
    gl.glBegin(GL2.GL_QUADS);
    gl.glVertex2f(x, y);
    gl.glVertex2f(x + w, y);
    gl.glVertex2f(x + w, y + h);
    gl.glVertex2f(x, y + h);
    gl.glEnd();
  }

  private void drawPart(GL2 gl, SkeletonJoint a, SkeletonJoint b) {
    NiJoint ja = skeleton.getJoint(a);
    NiJoint jb = skeleton.getJoint(b);
    if (ja != null && ja.getPosition() != null && jb != null && jb.getPosition() != null) {

      Vec3 pa = raw ? ja.getPositionRaw().over(1000) : ja.getPosition().over(1000);
      Vec3 pb = raw ? jb.getPositionRaw().over(1000) : jb.getPosition().over(1000);
      gl.glVertex3f(pa.x, pa.y, pa.z);
      gl.glVertex3f(pb.x, pb.y, pb.z);

    }
  }

  @Override
  public void skeletonUpdated(NiSkeleton skeleton) {
    this.skeleton = skeleton;
  }
}
