package edu.miami.cs.krobot.model.solid;

import javax.media.opengl.GL2;
import edu.miami.cs.js.jgloo.content.CMModel;
import edu.miami.cs.js.math.vector.Mat4;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.model.ModelPart;
import edu.miami.cs.krobot.view.MasterView;

public class PolygonMesh extends Solid {

  private boolean init;
  private String  file;
  private CMModel model;
  private Mat4    modelMatrix;
  private Vec3    translation = new Vec3(0);
  private Vec3    rotation    = new Vec3(0);
  private Vec3    scale       = new Vec3(1);
  
  public Mat4 getModelMatrix() {
    return modelMatrix;
  }

  public CMModel getModel() {
    return model;
  }
  
  public PolygonMesh() {
  }

  /**
   * @param name
   * @param parent
   * @param offset - translation from parent's center position to this object's center position
   * @param fileName - path to the model file on disk
   * @param translation - translation from this object's center position (to adjust models that
   *          aren't centered)
   * @param rotation - rotation around point (center + translation)
   * @param scale - scaling for x,y,z
   */
  public PolygonMesh(String name, ModelPart parent, Vec3 offset, String fileName, Vec3 translation,
      Vec3 rotation, Vec3 scale, float mass) {
    super(name, parent, offset, mass);
    this.file = fileName;
    this.translation = translation;
    this.rotation = rotation;
    this.scale = scale;
    updateLocalTransform();
  }

  @Override
  public void draw(GL2 gl) {
    if (!init) init(gl);
    model.get().render(gl, modelMatrix);
  }

  private void init(GL2 gl) {
    updateModelMatrix();
    model = MasterView.CONTENT.retrieveModel(file, false, gl);
    init = true;
  }

  public void setTranslation(Vec3 translation) {
    this.translation = translation;
    updateModelMatrix();
  }

  public void setRotation(Vec3 rotation) {
    this.rotation = rotation;
    updateModelMatrix();
  }

  public void setScale(Vec3 scale) {
    this.scale = scale;
    updateModelMatrix();
  }

  public void setFile(String file) {
    this.file = file;
  }

  public String getFile() {
    return file;
  }

  private void updateModelMatrix() {
    Mat4 t = Mat4.createTranslation(translation);
    Mat4 rx = Mat4.createRotationX(Math.toRadians(rotation.x));
    Mat4 ry = Mat4.createRotationY(Math.toRadians(rotation.y));
    Mat4 rz = Mat4.createRotationZ(Math.toRadians(rotation.z));
    Mat4 s = Mat4.createScale(scale);
    modelMatrix = t.times(rx).times(ry).times(rz).times(s);
  }

  public Vec3 getTranslation() {
    return translation;
  }

  public Vec3 getRotation() {
    return rotation;
  }

  public Vec3 getScale() {
    return scale;
  }

  @Override
  protected PolygonMesh clone(Model clone) {
    return new PolygonMesh(name,
        parent == null ? clone.getRoot() : clone.getPart(parent.getName()), offset.clone(), file,
        translation.clone(), rotation.clone(), scale.clone(), mass);
  }
}
