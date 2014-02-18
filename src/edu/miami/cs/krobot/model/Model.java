package edu.miami.cs.krobot.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import edu.miami.cs.js.math.vector.Mat4;
import edu.miami.cs.js.math.vector.Orientation;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.mapping.NiSkeletonMapper;
import edu.miami.cs.krobot.model.joint.Joint1DOF;
import edu.miami.cs.krobot.model.joint.PrismaticJoint;
import edu.miami.cs.krobot.model.joint.RevoluteJoint;
import edu.miami.cs.krobot.model.solid.Box;
import edu.miami.cs.krobot.model.solid.Cylinder;
import edu.miami.cs.krobot.model.solid.PolygonMesh;
import edu.miami.cs.krobot.model.solid.Solid;
import edu.miami.cs.krobot.model.solid.Sphere;
import edu.miami.cs.krobot.openni.NiSkeleton;

/**
 * Robot model consisting of joints and geometry.
 * 
 * @author justin
 */
public class Model {

  private List<ModelListener>     listeners      = new ArrayList<ModelListener>();
  private NiSkeleton              skeleton;
  private ModelPart               root;
  private String                  name;
  private List<ModelPart>         parts          = new ArrayList<ModelPart>();
  private Map<String, ModelPart>  partMap        = new HashMap<String, ModelPart>();
  private Mat4                    modelMatrix    = Mat4.createIdentity();
  private Map<Joint1DOF, Integer> jointIndices   = new HashMap<Joint1DOF, Integer>();
  private List<Joint1DOF>         joints         = new ArrayList<Joint1DOF>();
  private List<Solid>             geometry       = new ArrayList<Solid>();
  private Vec3                    centerOfMass;
  public String                   meshDir;
  public String                   materialDir;
  public String                   textureDir;
  public float                    scale          = 1;
  public Vec3                     forward        = new Vec3(0, 0, -1);
  public Vec3                     right          = new Vec3(1, 0, 0);
  public Vec3                     up             = new Vec3(0, 1, 0);
  private List<KinematicChain>    chains;
  public List<ModelPose>          poses;
  public ModelStatus              status;
  private Orientation             orientation;
  private NiSkeletonMapper        skeletonMapper = new NiSkeletonMapper.DummyMapper();

  public Model() {
    root = new RootPart("root", null, new Vec3(0, 0, 0));
    partMap.put(root.name, root);
  }

  public Model(String name) {
    this();
    this.name = name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public NiSkeletonMapper getMapper() {
    return skeletonMapper;
  }

  public void setChains(List<KinematicChain> chains) {
    this.chains = chains;

    for (KinematicChain chain : chains) {
      for (ModelPart part : chain.parts) {
        if (part != null) {
          partMap.put(part.name, part);
          parts.add(part);
          part.chain = chain;
        }
      }
    }
  }

  public List<KinematicChain> getChains() {
    return chains;
  }

  public String getName() {
    return name;
  }

  public ModelPart getRoot() {
    return root;
  }

  public Vec3 getCenterOfMass() {
    return centerOfMass;
  }

  public Map<Joint1DOF, Integer> getJointIndices() {
    return jointIndices;
  }

  public List<Joint1DOF> getJoints() {
    return joints;
  }

  public List<Solid> getGeometry() {
    return geometry;
  }

  public Mat4 getModelMatrix() {
    return modelMatrix;
  }

  public void setModelMatrix(Mat4 modelMatrix) {
    this.modelMatrix = modelMatrix;
  }

  public ModelPart getPart(String name) {
    return partMap.get(name);
  }

  public List<ModelPart> getBodies() {
    return parts;
  }

  public NiSkeleton getSkeleton() {
    return skeleton;
  }

  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
  }

  public void setSkeleton(NiSkeleton skeleton) {
    this.skeleton = skeleton;
  }

  public void addListener(ModelListener l) {
    listeners.add(l);
  }

  public void commitUpdate() {
    for (ModelListener l : listeners)
      l.modelCommitted(this);
  }

  public void setSkelMapper(String skelMapper) {
    try {
      skeletonMapper = (NiSkeletonMapper) Class.forName(skelMapper).newInstance();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void initialize(String modelDir) {

    if (modelDir != null) {
      meshDir = new File(modelDir, meshDir).getAbsolutePath();
      textureDir = new File(modelDir, textureDir).getAbsolutePath();
      materialDir = new File(modelDir, materialDir).getAbsolutePath();
    }

    for (ModelPart part : parts) {
      if (part.parent != null)
        part.parent.children.add(part);
      else
        root.children.add(part);

      if (part instanceof Joint1DOF) {
        Joint1DOF joint = (Joint1DOF) part;
        jointIndices.put(joint, joints.size());
        joints.add(joint);
      } else if (part instanceof Solid) {
        Solid geom = (Solid) part;
        geometry.add(geom);

        if (part instanceof PolygonMesh) {
          PolygonMesh p = (PolygonMesh) part;
          p.setScale(p.getScale().times(scale));
        }
      }
    }

    // if the model does not provide a status format, the status will simply be the joint
    // values in the order they were provided
    if (status == null) {
      Joint1DOF[] allJoints = new Joint1DOF[joints.size()];
      joints.toArray(allJoints);
      status = new ModelStatus(allJoints, true);
    }

    skeletonMapper.initialize(this);

    root.update();
  }

  public void update() {
    root.update();

    centerOfMass = new Vec3(0);
    float totalMass = 0;
    for (Solid solid : geometry) {
      float m = solid.getMass();
      centerOfMass.add(solid.getPosition().times(m));
      totalMass += m;
    }
    centerOfMass.div(totalMass);
  }

  /**
   * Creates an independent copy of this model (no references to the original).
   */
  public Model clone(String name) {
    Model cloneModel = new Model(name);
    cloneModel.parts = new ArrayList<ModelPart>();
    cloneChildren(root, cloneModel);
    cloneModel.initialize(null);
    return cloneModel;
  }

  private void cloneChildren(ModelPart body, Model cloneModel) {
    for (ModelPart child : body.getChildren()) {
      ModelPart cloneChild = child.clone(cloneModel);
      cloneModel.parts.add(cloneChild);
      cloneModel.partMap.put(cloneChild.name, cloneChild);
      cloneChildren(child, cloneModel);
    }
  }

  public static Model loadFromYAML(File file) {

    try {
      Constructor constructor = new Constructor(Model.class);
      constructor.addTypeDescription(new TypeDescription(RevoluteJoint.class, "!revolute"));
      constructor.addTypeDescription(new TypeDescription(PrismaticJoint.class, "!prismatic"));
      constructor.addTypeDescription(new TypeDescription(Cylinder.class, "!cylinder"));
      constructor.addTypeDescription(new TypeDescription(PolygonMesh.class, "!mesh"));
      constructor.addTypeDescription(new TypeDescription(Box.class, "!box"));
      constructor.addTypeDescription(new TypeDescription(Sphere.class, "!sphere"));
      constructor.addTypeDescription(new TypeDescription(KinematicChain.class, "!chain"));

      Yaml yaml = new Yaml(constructor);
      Model model = (Model) yaml.load(new FileInputStream(file));
      model.initialize(file.getParent());
      return model;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  public ModelStatus getStatus() {
    return status;
  }
}
