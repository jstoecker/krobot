package edu.miami.cs.krobot.view;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.StatusException;
import edu.miami.cs.js.jgloo.TexParams;
import edu.miami.cs.js.jgloo.Texture2D;
import edu.miami.cs.js.math.vector.Vec2;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.openni.KinectSensor;
import edu.miami.cs.krobot.openni.NiJoint;
import edu.miami.cs.krobot.openni.NiSkeleton;

public class DepthView extends GLView implements KinectSensor.Listener {

  private final static int MAX_DEPTH_SIZE = 10000;
  Texture2D                texture;
  KinectSensor             kinect;
  boolean                  updateTexture  = true;
  int                      maxDepth       = 0;
  float[]                  histogram      = new float[MAX_DEPTH_SIZE];
  ByteBuffer               pixels;

  public DepthView(String name, MasterView mainView, KinectSensor kinect) {
    super(name, mainView);
    this.kinect = kinect;
    kinect.addListener(this);
  }

  @Override
  public void init(GL2 gl) {
  }

  @Override
  public void dispose(GL2 gl) {
    if (texture != null) texture.dispose(gl);
  }

  @Override
  public void draw(GL2 gl) {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);

    if (!kinect.isActive()) return;

    if (updateTexture) updateTexture(gl);

    gl.glOrtho(0, texture.getWidth(), 0, texture.getHeight(), -1, 1);

    gl.glEnable(GL.GL_TEXTURE_2D);
    texture.bind(gl);
    gl.glColor3f(1, 1, 1);

    gl.glBegin(GL2.GL_QUADS);
    gl.glTexCoord2f(1, 1);
    gl.glVertex2f(0, 0);
    gl.glTexCoord2f(0, 1);
    gl.glVertex2f(texture.getWidth(), 0);
    gl.glTexCoord2f(0, 0);
    gl.glVertex2f(texture.getWidth(), texture.getHeight());
    gl.glTexCoord2f(1, 0);
    gl.glVertex2f(0, texture.getHeight());
    gl.glEnd();
    Texture2D.unbind(gl);
    gl.glDisable(GL.GL_TEXTURE_2D);

    drawSkeletons(gl);
  }

  private void drawSkeletons(GL2 gl) {
    gl.glEnable(GL2.GL_POINT_SMOOTH);
    gl.glEnable(GL.GL_LINE_SMOOTH);
    gl.glEnable(GL.GL_BLEND);
    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

    for (NiSkeleton skeleton : kinect.getSkeletons()) {
      gl.glColor4f(1, 0.35f, 0.35f, 0.75f);
      drawSkeleton(gl, skeleton, true);
      gl.glColor4f(0.35f, 0.35f, 1, 0.75f);
      drawSkeleton(gl, skeleton, false);
    }

    gl.glColor4f(1, 1, 1, 1);
    gl.glDisable(GL.GL_BLEND);

    gl.glLineWidth(1);
    gl.glPointSize(1);
  }

  private void drawSkelJoints(GL2 gl, Skeleton2D skel, boolean raw) {
    gl.glPointSize(16);
    gl.glBegin(GL2.GL_POINTS);
    for (Vec2 p : (raw ? skel.rawPositions.values() : skel.filteredPositions.values()))
      if (p != null) gl.glVertex2f(p.x, p.y);
    gl.glEnd();
  }

  private void drawSkelParts(GL2 gl, Skeleton2D skel, boolean raw) {
    gl.glLineWidth(8);
    gl.glBegin(GL2.GL_LINES);
    drawPart(gl, skel, SkeletonJoint.TORSO, SkeletonJoint.RIGHT_SHOULDER, raw);
    drawPart(gl, skel, SkeletonJoint.TORSO, SkeletonJoint.LEFT_SHOULDER, raw);
    drawPart(gl, skel, SkeletonJoint.TORSO, SkeletonJoint.RIGHT_HIP, raw);
    drawPart(gl, skel, SkeletonJoint.TORSO, SkeletonJoint.LEFT_HIP, raw);
    drawPart(gl, skel, SkeletonJoint.NECK, SkeletonJoint.HEAD, raw);
    drawPart(gl, skel, SkeletonJoint.NECK, SkeletonJoint.RIGHT_SHOULDER, raw);
    drawPart(gl, skel, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.RIGHT_ELBOW, raw);
    drawPart(gl, skel, SkeletonJoint.RIGHT_ELBOW, SkeletonJoint.RIGHT_HAND, raw);
    drawPart(gl, skel, SkeletonJoint.NECK, SkeletonJoint.LEFT_SHOULDER, raw);
    drawPart(gl, skel, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_ELBOW, raw);
    drawPart(gl, skel, SkeletonJoint.LEFT_ELBOW, SkeletonJoint.LEFT_HAND, raw);
    drawPart(gl, skel, SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE, raw);
    drawPart(gl, skel, SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT, raw);
    drawPart(gl, skel, SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE, raw);
    drawPart(gl, skel, SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT, raw);
    drawPart(gl, skel, SkeletonJoint.RIGHT_HIP, SkeletonJoint.LEFT_HIP, raw);
    gl.glEnd();
  }

  private void drawSkeleton(GL2 gl, NiSkeleton skeleton3D, boolean raw) {
    Skeleton2D skeleton = new Skeleton2D(skeleton3D);
    try {
      skeleton.update(kinect.getDepthGenerator());
      drawSkelJoints(gl, skeleton, raw);
      drawSkelParts(gl, skeleton, raw);
    } catch (StatusException e) {
      e.printStackTrace();
    }
  }

  private void drawPart(GL2 gl, Skeleton2D skel2D, SkeletonJoint a, SkeletonJoint b, boolean raw) {
    NiJoint ja = skel2D.skeleton3D.getJoint(a);
    NiJoint jb = skel2D.skeleton3D.getJoint(b);

    Vec2 pa = raw ? skel2D.rawPositions.get(ja) : skel2D.filteredPositions.get(ja);
    Vec2 pb = raw ? skel2D.rawPositions.get(jb) : skel2D.filteredPositions.get(jb);

    if (pa != null && pb != null) {
      gl.glVertex2f(pa.x, pa.y);
      gl.glVertex2f(pb.x, pb.y);
    }
  }

  private void updateTexture(GL2 gl) {
    DepthMetaData depthMD = kinect.getDepthGenerator().getMetaData();
    ShortBuffer depthBuf = depthMD.getData().createShortBuffer();

    calcHistogram(depthBuf);
    depthBuf.rewind();

    if (texture == null) {
      pixels = ByteBuffer.allocate(depthMD.getFullXRes() * depthMD.getFullYRes());
      TexParams params = new TexParams(GL.GL_TEXTURE_2D);
      params.minFilter(GL.GL_LINEAR);
      params.magFilter(GL.GL_LINEAR);
      texture = new Texture2D(gl, depthMD.getFullXRes(), depthMD.getFullYRes(), 0, GL.GL_LUMINANCE,
          0, GL.GL_LUMINANCE, GL.GL_UNSIGNED_BYTE, params);
    }
    while (depthBuf.hasRemaining())
      pixels.put((byte) histogram[depthBuf.get()]);
    pixels.rewind();

    texture.buffer(gl, pixels);

    updateTexture = false;
  }

  private void calcHistogram(ShortBuffer depthBuf) {
    for (int i = 0; i <= maxDepth; i++)
      histogram[i] = 0;

    int numPoints = 0;
    maxDepth = 0;
    while (depthBuf.hasRemaining()) {
      short val = depthBuf.get();
      if (val > maxDepth) maxDepth = val;
      if ((val != 0) && (val < MAX_DEPTH_SIZE)) {
        histogram[val]++;
        numPoints++;
      }
    }

    for (int i = 1; i <= maxDepth; i++)
      histogram[i] += histogram[i - 1];

    if (numPoints > 0) {
      for (int i = 1; i <= maxDepth; i++)
        histogram[i] = (int) (256 * (1.0f - (histogram[i] / (float) numPoints)));
    }
  }

  @Override
  public void kinectUpdated(KinectSensor sensor) {
    updateTexture = true;
    repaint();
  }

  private class Skeleton2D {
    NiSkeleton             skeleton3D;
    HashMap<NiJoint, Vec2> rawPositions      = new HashMap<NiJoint, Vec2>();
    HashMap<NiJoint, Vec2> filteredPositions = new HashMap<NiJoint, Vec2>();

    public Skeleton2D(NiSkeleton skeleton) {
      this.skeleton3D = skeleton;
    }

    void update(DepthGenerator dg) throws StatusException {

      int w = dg.getMetaData().getFullXRes();
      int h = dg.getMetaData().getFullYRes();

      for (NiJoint joint : skeleton3D.getJoints()) {
        Vec3 p3D = joint.getPositionRaw();
        Point3D p2D = dg.convertRealWorldToProjective(new Point3D(p3D.x, p3D.y, p3D.z));
        rawPositions.put(joint, new Vec2(w - p2D.getX() - 1, h - p2D.getY() - 1));

        p3D = joint.getPosition();
        p2D = dg.convertRealWorldToProjective(new Point3D(p3D.x, p3D.y, p3D.z));
        filteredPositions.put(joint, new Vec2(w - p2D.getX() - 1, h - p2D.getY() - 1));
      }
    }
  }
}
