package edu.miami.cs.krobot.mapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import edu.miami.cs.js.math.vector.Mat4;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.js.math.vector.Vec4;
import edu.miami.cs.krobot.mapping.NaoH21HipTable.HipConfig;
import edu.miami.cs.krobot.model.Model;

/**
 * This class stores pre-computed leg plane normal vectors corresponding to a sampling of all
 * possible hip_yawpitch and hip_roll angles. The user can query which angles best match a given leg
 * plane normal. This tree is constructed using forward kinematics in a pre-processing step and
 * serialized to disk for use later. Then, during real-time mapping, the user can easily calculate
 * the leg normal from the skeleton and retrieve the necessary angles that orient the plane from
 * this tree.
 * 
 * @author justin
 */
public class Nao3DHipTable implements Serializable {

  private final HipConfig[] rightLeg;
  private final HipConfig[] leftLeg;

  public Nao3DHipTable(HipConfig[] configs, HipConfig[] leftLeg) {
    this.rightLeg = configs;
    this.leftLeg = leftLeg;
  }

  /**
   * Returns the leg configuration that is closest to the given x, y, z components of the current
   * leg plane. MAKE SURE (X,Y,Z) IS NORMALIZED!
   */
  public HipConfig getBestConfiguration(float x, float y, float z, boolean left) {
    
    HipConfig[] table = left ? leftLeg : rightLeg;
    
    float maxDotProd = Float.NEGATIVE_INFINITY;
    HipConfig best = null;

    for (int i = 0; i < table.length; i++) {
      HipConfig c = table[i];
      float dotProd = x * c.normal.x + y * c.normal.y + z * c.normal.z;
      if (dotProd > maxDotProd) {
        maxDotProd = dotProd;
        best = c;
      }
    }

    if (maxDotProd < 0.6) {
      return null;
    }
    
    return best;
  }
  
  public HipConfig getBestConfiguration(Vec3 v, boolean left) {
    return getBestConfiguration(v.x, v.y, v.z, left);
  }

  public void write(String fileName) {
    try {
      new ObjectOutputStream(new FileOutputStream(fileName)).writeObject(this);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Nao3DHipTable read(String fileName) {
    try {
      return (Nao3DHipTable) (new ObjectInputStream(new FileInputStream(fileName))
          .readObject());
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Constructs the tree by running forward kinematics for possible all possible configurations with
   * a given sampling rate.
   * 
   * @param nao - Nao simulation 3D model
   * @param samplesPerDegree - the change in degrees per sample. There will be approximately
   *          90*70*samplesPerDegree total samples.
   * @return
   */
  public static Nao3DHipTable build(Nao3D nao, float samplesPerDegree) {

    HipConfig[] right = buildRightLeg(nao, samplesPerDegree);
    HipConfig[] left = buildLeftLeg(nao, samplesPerDegree);
    
    return new Nao3DHipTable(right, left);
  }
  
  private static HipConfig[] buildRightLeg(Nao3D nao, float samplesPerDegree) {
    int yawPitchSamples = (int) (samplesPerDegree * nao.rHipYawPitch.getRange());
    float yawPitchStep = nao.rHipYawPitch.getRange() / yawPitchSamples;

    int rollSamples = (int) (samplesPerDegree * nao.rHipRoll.getRange());
    float rollStep = nao.rHipRoll.getRange() / rollSamples;

    HipConfig[] rightLeg = new HipConfig[yawPitchSamples * rollSamples];
    
    int k = 0;
    float yawPitch = nao.rHipYawPitch.getMinValue();
    
    for (int i = 0; i < yawPitchSamples; i++) {
      float roll = nao.rHipRoll.getMinValue();

      for (int j = 0; j < rollSamples; j++) {
        
        Mat4 rotation = Mat4.createRotation(Math.toRadians(yawPitch), new Vec3(-1, 0, 1).normalize());
        rotation = rotation.times(Mat4.createRotationY(Math.toRadians(roll)));
        Vec3 normal = rotation.transform(1, 0, 0);
        Vec3 thigh = rotation.transform(0, 0, -1);
        
        rightLeg[k++] = new HipConfig(normal, thigh, yawPitch, roll);
        roll += rollStep;
      }
      
      yawPitch += yawPitchStep;
    }
    
    return rightLeg;
  }
  
  private static HipConfig[] buildLeftLeg(Nao3D nao, float samplesPerDegree) {
    int yawPitchSamples = (int) (samplesPerDegree * nao.lHipYawPitch.getRange());
    float yawPitchStep = nao.lHipYawPitch.getRange() / yawPitchSamples;

    int rollSamples = (int) (samplesPerDegree * nao.lHipRoll.getRange());
    float rollStep = nao.lHipRoll.getRange() / rollSamples;

    HipConfig[] leftLeg = new HipConfig[yawPitchSamples * rollSamples];
    
    int k = 0;
    float yawPitch = nao.lHipYawPitch.getMinValue();
    
    for (int i = 0; i < yawPitchSamples; i++) {
      float roll = nao.lHipRoll.getMinValue();

      for (int j = 0; j < rollSamples; j++) {
        
        Mat4 rotation = Mat4.createRotation(Math.toRadians(yawPitch), new Vec3(-1, 0, -1).normalize());
        rotation = rotation.times(Mat4.createRotationY(Math.toRadians(roll)));
        Vec3 normal = rotation.transform(1, 0, 0);
        Vec3 thigh = rotation.transform(0, 0, -1);
        
        leftLeg[k++] = new HipConfig(normal, thigh, yawPitch, roll);
        roll += rollStep;
      }
      
      yawPitch += yawPitchStep;
    }
    
    return leftLeg;
  }
  
  public static class HipConfig implements Serializable {
    private final float hipYawPitch, hipRoll;
    private final Vec3 normal, thigh;
    
    public HipConfig(Vec3 normal, Vec3 thigh, float hipYawPitch, float hipRoll) {
      this.normal = normal;
      this.thigh = thigh;
      this.hipYawPitch = hipYawPitch;
      this.hipRoll = hipRoll;
    }
    
    public float getYawPitch() { return hipYawPitch; }
    public float getRoll() { return hipRoll; }
    public Vec3 getThigh() { return thigh; }
    public Vec3 getNormal() { return normal; }
    
  }
  
  public static void main(String[] args) {
    Model m = Model.loadFromYAML(new File("/Users/justin/projects/krobot/models/nao_rcss3d/nao.yml"));
    Nao3DHipTable t = Nao3DHipTable.build(new Nao3D(m), 1);
    
    HipConfig c = t.getBestConfiguration(new Vec3(1,0,0), true);
    
    System.out.println(c.hipYawPitch);
    System.out.println(c.hipRoll);
  }

  // ------------------------------------------------------------------------- //
}
