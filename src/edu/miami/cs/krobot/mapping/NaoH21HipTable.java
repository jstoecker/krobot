package edu.miami.cs.krobot.mapping;

import java.io.File;
import edu.miami.cs.js.math.vector.Mat4;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.js.math.vector.Vec4;
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
public class NaoH21HipTable {

  final HipConfig[] rightLeg;
  final HipConfig[] leftLeg;

  public NaoH21HipTable(HipConfig[] configs, HipConfig[] leftLeg) {
    this.rightLeg = configs;
    this.leftLeg = leftLeg;
  }


  /**
   * Returns the leg configuration that is closest to the given x, y, z components of the current
   * leg plane. MAKE SURE NORMAL IS NORMALIZED!
   */
  public HipConfig getBestConfiguration(Vec3 normal, boolean left) {

    HipConfig[] table = left ? leftLeg : rightLeg;

    float maxDotProd = Float.NEGATIVE_INFINITY;
    HipConfig best = null;

    for (int i = 0; i < table.length; i++) {
      HipConfig c = table[i];
      float dotProd = normal.dot(c.normal);
      if (dotProd > maxDotProd) {
        maxDotProd = dotProd;
        best = c;
      }
    }
    
    return best;
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
  public static NaoH21HipTable build(NaoH21 nao, float samplesPerDegree) {

    HipConfig[] right = buildRightLeg(nao, samplesPerDegree);
    HipConfig[] left = buildLeftLeg(nao, samplesPerDegree);

    return new NaoH21HipTable(right, left);
  }

  private static HipConfig[] buildRightLeg(NaoH21 nao, float samplesPerDegree) {
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
        Mat4 rotation = Mat4.createRotation(Math.toRadians(yawPitch), new Vec3(0,1,1).normalize());
        rotation = rotation.times(Mat4.createRotationX(Math.toRadians(roll)));
        Vec3 normal = rotation.transform(0, -1, 0);
        Vec3 thigh = rotation.transform(0, 0, -1);
        rightLeg[k++] = new HipConfig(normal,thigh, yawPitch, roll);
        roll += rollStep;
      }
      yawPitch += yawPitchStep;
    }

    return rightLeg;
  }

  private static HipConfig[] buildLeftLeg(NaoH21 nao, float samplesPerDegree) {
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
        
        Mat4 rotation = Mat4.createRotation(Math.toRadians(yawPitch), new Vec3(0,1,-1).normalize());
        rotation = rotation.times(Mat4.createRotationX(Math.toRadians(roll)));
        Vec3 normal = rotation.transform(0, -1, 0);
        Vec3 thigh = rotation.transform(0, 0, -1);

        leftLeg[k++] = new HipConfig(normal, thigh, yawPitch, roll);
        roll += rollStep;
      }

      yawPitch += yawPitchStep;
    }

    return leftLeg;
  }

  public static class HipConfig {
    private final float hipYawPitch, hipRoll;
    private final Vec3 normal;
    private final Vec3 thigh;

    public HipConfig(Vec3 normal, Vec3 thigh, float hipYawPitch,
        float hipRoll) {
      this.normal = normal;
      this.thigh = thigh;
      this.hipYawPitch = hipYawPitch;
      this.hipRoll = hipRoll;
    }

    public float getYawPitch() {
      return hipYawPitch;
    }

    public float getRoll() {
      return hipRoll;
    }

    public Vec3 getNormal() {
      return normal;
    }
    
    public Vec3 getThigh() {
      return thigh;
    }
  }
  
  public static void main(String[] args) {
    Model m = Model.loadFromYAML(new File("/Users/justin/projects/krobot/models/nao_v4_h21/nao.yml"));
    NaoH21HipTable t = NaoH21HipTable.build(new NaoH21(m), 1);
    
    HipConfig c = t.getBestConfiguration(new Vec3(0,-1,0), false);
    
//    for (HipConfig hc : t.rightLeg) {
//      System.out.printf("%f  %f   %s\n", hc.hipYawPitch, hc.hipRoll, hc.normal);
//    }
    
    System.out.println(c.hipYawPitch);
    System.out.println(c.hipRoll);
  }
}
