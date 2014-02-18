package edu.miami.cs.krobot.view;

import java.nio.ByteBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import edu.miami.cs.js.jgloo.FrameBufferObject;
import edu.miami.cs.js.jgloo.RenderBuffer;
import edu.miami.cs.js.jgloo.ShaderProgram;
import edu.miami.cs.js.jgloo.Texture2D;
import edu.miami.cs.js.jgloo.Texture2D.Filtering;
import edu.miami.cs.js.jgloo.Texture2D.Wrap;
import edu.miami.cs.js.jgloo.draw.Draw;
import edu.miami.cs.js.jgloo.light.DirLight;
import edu.miami.cs.js.jgloo.light.LightModel;
import edu.miami.cs.js.jgloo.light.Material;
import edu.miami.cs.js.jgloo.view.OrbitCamera;
import edu.miami.cs.js.math.vector.Mat4;
import edu.miami.cs.js.math.vector.Orientation;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.Config;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.ModelPart;
import edu.miami.cs.krobot.model.joint.Joint1DOF;
import edu.miami.cs.krobot.model.joint.RevoluteJoint;
import edu.miami.cs.krobot.model.solid.Box;
import edu.miami.cs.krobot.model.solid.Cylinder;
import edu.miami.cs.krobot.model.solid.PolygonMesh;
import edu.miami.cs.krobot.model.solid.Solid;
import edu.miami.cs.krobot.model.solid.Sphere;
import edu.miami.cs.krobot.util.Maths;

public class RobotView extends GLView {

  FrameBufferObject depthFBO;
  ShaderProgram     depthShader;
  ShaderProgram     sceneShader;
  Texture2D         blankTex;
  Mat4              lightView;
  Mat4              lightProj;
  Vec3              modelCenter;
  Material          floorMaterial;

  static boolean    Y_UP          = false;
  boolean           drawWireframe = false;
  Model             model;
  Model             unconstrainedModel;
  OrbitCamera       camera;
  Joint1DOF         selectedJoint;
  boolean           shadows       = true;
  int               shadowSize;

  LightModel        lighting;

  public RobotView(String name, Model model, MasterView mainView, Config config) {
    super(name, mainView);
    this.model = model;
    this.shadows = config.shadows;
    this.shadowSize = config.shadowSize;
    if (drawWireframe) createUnconstrainedCloneModel();

    model.update();

    modelCenter = new Vec3(0);
    int positions = 0;
    for (Solid solid : model.getGeometry()) {
      modelCenter.add(solid.getPosition());
      positions++;
    }
    modelCenter.div(positions);

    camera = new OrbitCamera(modelCenter.plus(model.forward), modelCenter, model.up.dot(new Vec3(0,
        1, 0)) > 0.99);
  }

  public void setSelectedJoint(Joint1DOF selectedJoint) {
    this.selectedJoint = selectedJoint;
  }

  public void setDrawWireframe(boolean drawWireframe) {
    this.drawWireframe = drawWireframe;
  }

  public OrbitCamera getCamera() {
    return camera;
  }

  private void createUnconstrainedCloneModel() {
    unconstrainedModel = model.clone(model.getName() + " (Unconstrained)");
    for (ModelPart body : unconstrainedModel.getBodies()) {
      if (body instanceof Joint1DOF) {
        final Joint1DOF cloneJoint = (Joint1DOF) body;
        cloneJoint.setMinValue(Float.NEGATIVE_INFINITY);
        cloneJoint.setMaxValue(Float.POSITIVE_INFINITY);

        Joint1DOF originalJoint = (Joint1DOF) model.getPart(cloneJoint.getName());
        originalJoint.addListener(new Joint1DOF.Listener() {
          public void valueChanged(Joint1DOF joint, float newValue) {
            cloneJoint.setValue(joint.getUnconstrainedValue());
            System.out.println("change");
          }
        });
      }
    }
  }

  @Override
  public void dispose(GL2 gl) {
    Box.dispose(gl);
    Cylinder.dispose(gl);
    Sphere.dispose(gl);

    sceneShader.dispose(gl);
    blankTex.dispose(gl);
    if (shadows) {
      depthFBO.dispose(gl);
      depthShader.dispose(gl);
    }
  }

  @Override
  public void init(GL2 gl) {
    ClassLoader cl = getClass().getClassLoader();

    floorMaterial = new Material();
    Vec3 lightDir = model.forward.times(0.5f).plus(model.right.times(-0.3f)).plus(model.up.times(0.5f))
        .normalize();

    if (shadows) {
      Texture2D depthTexture = Texture2D.create(gl, Filtering.NEAREST, Wrap.CLAMP_TO_EDGE,
          shadowSize, shadowSize, GL2GL3.GL_RED, GL2GL3.GL_RED, GL.GL_UNSIGNED_BYTE, null);

      sceneShader = ShaderProgram.create(gl, "shaders/phong_shadow.vs", "shaders/phong_shadow.fs",
          cl);
      depthShader = ShaderProgram.create(gl, "shaders/depth.vs", "shaders/depth.fs", cl);

      depthFBO = new FrameBufferObject(gl);
      depthFBO.bind(gl);
      depthFBO.attachColorTarget(gl, depthTexture, 0, 0, true);
      depthFBO.attachDepthTarget(gl, RenderBuffer.createDepthBuffer(gl, shadowSize, shadowSize),
          true);
      depthFBO.unbind(gl);

      Vec3 lightPos = modelCenter.plus(lightDir.times(2));
      Vec3 lightUp = model.forward.cross(lightDir).normalize();

      Mat4 lightBias = Mat4.createScale(new Vec3(0.5f)).times(Mat4.createTranslation(1, 1, 1));
      lightView = Mat4.createLookAt(lightPos.x, lightPos.y, lightPos.z, modelCenter.x,
          modelCenter.y, modelCenter.z, lightUp.x, lightUp.y, lightUp.z);
      lightProj = Mat4.createOrtho(-0.4f, 0.4f, -0.4f, 0.4f, 0, 4);
      Mat4 lvpb = lightBias.times(lightProj.times(lightView));

      sceneShader.enable(gl);
      gl.glUniformMatrix4fv(sceneShader.getUniform(gl, "lightViewProjBias"), 1, false,
          lvpb.values(), 0);
      gl.glUniform1i(sceneShader.getUniform(gl, "diffuseTex"), 0);
      gl.glUniform1i(sceneShader.getUniform(gl, "depthTex"), 1);
      gl.glUniform1i(sceneShader.getUniform(gl, "numLights"), 2);
      sceneShader.disable(gl);

    } else {
      sceneShader = ShaderProgram.create(gl, "shaders/phong.vs", "shaders/phong.fs", cl);
    }
    
    lighting = new LightModel();
    DirLight frontLight = new DirLight(lightDir.normalize());
    frontLight.setDiffuse(1, 1, 1, 1);
    frontLight.setSpecular(1, 1, 1, 1);
    lighting.addLight(frontLight);
    DirLight backLight = new DirLight(model.forward.times(-1));
    backLight.setAmbient(0, 0, 0, 0);
    backLight.setDiffuse(0.4f, 0.4f, 0.4f, 1);
    backLight.setSpecular(0, 0, 0, 0);
    lighting.addLight(backLight);
    lighting.setGlobalAmbient(0.3f, 0.3f, 0.3f, 1);
    lighting.apply(gl);

    ByteBuffer pixelData = ByteBuffer.allocate(4);
    pixelData.put((byte) 255);
    pixelData.put((byte) 255);
    pixelData.put((byte) 255);
    pixelData.put((byte) 255);
    pixelData.rewind();
    blankTex = Texture2D.create(gl, Filtering.NEAREST, Wrap.CLAMP_TO_EDGE, 1, 1, GL.GL_RGBA,
        GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, pixelData);

    Box.init(gl);
    Cylinder.init(gl);
    Sphere.init(gl);
  }

  private void drawDepthMap(GL2 gl) {

    gl.glDisable(GL.GL_SCISSOR_TEST);

    depthShader.enable(gl);

    gl.glClearColor(1, 1, 1, 1);
    gl.glEnable(GL.GL_DEPTH_TEST);
    depthFBO.bind(gl);
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    gl.glViewport(0, 0, shadowSize, shadowSize);

    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadMatrixf(lightProj.values(), 0);
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glLoadMatrixf(lightView.values(), 0);

    drawBody(gl, model.getRoot(), 1, 1, 1, lightView);
    depthFBO.unbind(gl);

    depthShader.disable(gl);
    gl.glActiveTexture(GL.GL_TEXTURE1);
    depthFBO.getColorTexture(0).bind(gl);
    gl.glActiveTexture(GL.GL_TEXTURE0);

    gl.glEnable(GL.GL_SCISSOR_TEST);
  }

  private void drawFloor(GL2 gl) {
    blankTex.bind(gl);
    gl.glColor4f(1, 1, 1, 1);
    floorMaterial.apply(gl);

    if (shadows) {
      int loc = sceneShader.getUniform(gl, "modelMatrix");
      gl.glUniformMatrix4fv(loc, 1, false, Mat4.createIdentity().values(), 0);
    }

    gl.glBegin(GL2.GL_QUADS);
    gl.glNormal3f(0, 0, 1);
    gl.glVertex3f(-1, -1, 0);
    gl.glVertex3f(1, -1, 0);
    gl.glVertex3f(1, 1, 0);
    gl.glVertex3f(-1, 1, 0);
    gl.glEnd();
  }

  @Override
  public void draw(GL2 gl) {
    model.update();
    if (unconstrainedModel != null) unconstrainedModel.update();

    if (shadows) drawDepthMap(gl);

    gl.glClearColor(0.9f, 0.9f, 0.9f, 1.0f);
    // gl.glClearColor(1, 1, 1, 1);
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    camera.setViewport(viewport);
    camera.apply(gl);

    lighting.apply(gl);
    
    gl.glEnable(GL.GL_DEPTH_TEST);

    gl.glPushMatrix();
    gl.glRotated(90, 1, 0, 0);
    gl.glColor3f(0.3f, 0.3f, 0.3f);
    gl.glLineWidth(1);
    Draw.grid(gl, 1, 0.1);
    gl.glPopMatrix();

    if (model != null) {
      sceneShader.enable(gl);

      gl.glEnable(GL.GL_DEPTH_TEST);
      gl.glEnable(GL.GL_CULL_FACE);
      gl.glCullFace(GL.GL_BACK);
      gl.glColor4f(1, 1, 1, 1);
      drawBody(gl, model.getRoot(), 1, 1, 1, camera.getViewMatrix());
      drawFloor(gl);

      sceneShader.disable(gl);

      gl.glDisable(GL2.GL_LIGHTING);
      gl.glDisable(GL.GL_TEXTURE_2D);
      if (drawWireframe) {
        gl.glLineWidth(2);
        gl.glEnable(GL2.GL_POLYGON_OFFSET_LINE);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
        gl.glPolygonOffset(4, 33);

        drawBody(gl, unconstrainedModel.getRoot(), 0, 0, 0, camera.getViewMatrix());
        gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
      }
      gl.glDisable(GL.GL_DEPTH_TEST);
      gl.glDisable(GL.GL_CULL_FACE);
      gl.glLineWidth(1);
    }

    gl.glColor3f(0.8f, 0.8f, 0.8f);

    // drawMappingVecs(gl);
  }

  private void drawMappingVecs(GL2 gl) {

    // this is only for illustrating the mapping algorithm for the NAO robot
    // and should be disabled with other models

    Vec3 shoulder = model.getPart("LShoulderPitch").getPosition();
    Vec3 elbow = model.getPart("LElbowRoll").getPosition();
    Mat4 m = model.getPart("Left Forearm/Hand").getGlobalTransform();
    Vec3 elbowb = m.transform(0, 0, 0);
    m = m.times(Mat4.createTranslation(.1f, 0, 0));
    Vec3 forearm = m.transform(0, 0, 0).minus(elbowb);
    Vec3 hand = elbow.plus(forearm);
    Vec3 bicep = elbow.minus(shoulder);
    Vec3 bicepProjected = Maths.project(bicep, Vec3.unitX(), Vec3.unitZ());
    Vec3 bicepNormal = bicep.cross(bicepProjected).normalize().times(-.1f);
    Vec3 forearmNormal = bicep.cross(forearm).normalize().times(-.1f);

    Vec3 hip = model.getPart("RHipYawPitch").getPosition();
    Vec3 knee = model.getPart("Right Tibia").getPosition();
    Vec3 foot = model.getPart("Right Foot").getPosition();

    Vec3 thigh = knee.minus(hip);
    Vec3 tibia = foot.minus(knee);

    gl.glLineWidth(7);
    gl.glBegin(GL2.GL_LINES);
    {
      // basis
      gl.glColor3f(0, 0, 0);
      glVec3(gl, shoulder);
      glVec3(gl, shoulder.plus(Vec3.unitX().times(0.1f)));
      glVec3(gl, shoulder);
      glVec3(gl, shoulder.plus(Vec3.unitY().times(0.1f)));
      glVec3(gl, shoulder);
      glVec3(gl, shoulder.plus(Vec3.unitZ().times(0.1f)));

      glVec3(gl, hip);
      glVec3(gl, hip.plus(Vec3.unitX().times(0.1f)));
      glVec3(gl, hip);
      glVec3(gl, hip.plus(Vec3.unitY().times(-0.1f)));
      glVec3(gl, hip);
      glVec3(gl, hip.plus(Vec3.unitZ().times(-0.1f)));

      // bicep and bicep normal and projection
      gl.glColor3f(1, 0.3f, 0.3f);
      glVec3(gl, shoulder);
      glVec3(gl, elbow);
      glVec3(gl, shoulder);
      glVec3(gl, shoulder.plus(bicepNormal));
      glVec3(gl, shoulder);
      glVec3(gl, shoulder.plus(bicepProjected));
      glVec3(gl, elbow);
      glVec3(gl, shoulder.plus(bicepProjected));

      // forearm and forearm normal
      gl.glColor3f(0.3f, 1, 0.3f);
      glVec3(gl, elbow);
      glVec3(gl, hand);
      glVec3(gl, elbow);
      glVec3(gl, elbow.plus(forearmNormal));
      gl.glColor3f(1, 0.3f, 0.3f);
      glVec3(gl, elbow);
      glVec3(gl, elbow.plus(bicep));
      glVec3(gl, elbow);
      glVec3(gl, elbow.plus(bicepNormal));

      // right leg
      gl.glColor3f(1, 0.3f, 0.3f);
      glVec3(gl, hip);
      glVec3(gl, knee);
      glVec3(gl, knee);
      glVec3(gl, knee.plus(thigh));

      gl.glColor3f(0.3f, 1, 0.3f);
      glVec3(gl, knee);
      glVec3(gl, foot);

      gl.glColor3f(0.3f, 0.3f, 1);
      float yawpitchangle = ((RevoluteJoint) model.getPart("RHipYawPitch")).getRadians();
      float rollangle = ((RevoluteJoint) model.getPart("RHipRoll")).getRadians();
      Mat4 rotation = Mat4.createRotation(yawpitchangle, new Vec3(0, 1, 1).normalize());
      rotation = rotation.times(Mat4.createRotationX(rollangle));
      Vec3 legright = rotation.transform(0, -1, 0);
      Vec3 legforward = rotation.transform(0, 0, -1);
      glVec3(gl, hip);
      glVec3(gl, hip.plus(legright.times(.1f)));
      glVec3(gl, hip);
      glVec3(gl, hip.plus(legforward.times(.1f)));
    }
    gl.glEnd();

    gl.glColor3f(0, 0, 0);
    gl.glPointSize(15);
    gl.glBegin(GL2.GL_POINTS);
    // gl.glVertex3f(shoulder.x, shoulder.y, shoulder.z);
    // gl.glVertex3f(elbow.x, elbow.y, elbow.z);
    // gl.glVertex3f(hand.x, hand.y, hand.z);
    //
    gl.glVertex3f(hip.x, hip.y, hip.z);
    gl.glVertex3f(knee.x, knee.y, knee.z);
    gl.glVertex3f(foot.x, foot.y, foot.z);

    gl.glEnd();
  }

  private static void glVec3(GL2 gl, Vec3 v) {
    gl.glVertex3f(v.x, v.y, v.z);
  }

  private Vec3 getHighlight(ModelPart part) {
    if (selectedJoint == null || selectedJoint.getChildren() == null) return null;

    if (selectedJoint.getChain() == part.getChain()) return new Vec3(0.5f, 1, 0.3f);

    return null;
  }

  private void drawBody(GL2 gl, ModelPart body, float r, float g, float b, Mat4 viewMatrix) {
    if (body instanceof Solid) {
      Vec3 c = getHighlight(body);
      if (c == null) c = new Vec3(r, g, b);
      gl.glColor3f(c.x, c.y, c.z);

      // float angle = 0;
      // if (model.getSkeleton() != null) {
      // Orientation o = model.getSkeleton().getLowerOrientation();
      // if (o != null) {
      // angle = Maths.angle(Vec3.unitY(), o.getUp(), o.getForward());
      // System.out.println(angle);
      // }
      // }
      // Mat4 modelRot = Mat4.createTranslation(0, 0,
      // .333094f).times(Mat4.createRotationX(angle).times(Mat4.createTranslation(0,0,-.33309f)));

      if (shadows) {
        Mat4 modelMat = body.getGlobalTransform();
        if (body instanceof PolygonMesh) {
          modelMat = modelMat.times(((PolygonMesh) body).getModelMatrix());
        }

        int loc = sceneShader.getUniform(gl, "modelMatrix");
        gl.glUniformMatrix4fv(loc, 1, false, modelMat.values(), 0);
      }

      gl.glPushMatrix();
      gl.glLoadIdentity();
      gl.glLoadMatrixf(viewMatrix.values(), 0);
      gl.glMultMatrixf(body.getGlobalTransform().values(), 0);
      body.draw(gl);
      gl.glPopMatrix();
    }
    for (ModelPart child : body.getChildren())
      drawBody(gl, child, r, g, b, viewMatrix);
  }
}
