package edu.miami.cs.krobot.openni;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import edu.miami.cs.js.math.vector.Vec3;

/**
 * Records joint positions directly from OpenNI camera space over time.
 * 
 * @author justin
 */
public class NiSkeletonRecorder implements NiSkeletonSrcListener {

  private boolean        active = false;
  private BufferedWriter bw;

  public NiSkeletonRecorder() {
  }

  public void enable(String fileName) {
    active = true;
    try {
      bw = new BufferedWriter(new FileWriter(new File(fileName)));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void disable() {
    active = false;
    try {
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void writeVec3(Vec3 v) throws IOException {
    if (v == null)
      bw.write("NaN NaN NaN ");
    else
      bw.write(String.format("%.4f %.4f %.4f ", v.x, v.y, v.z));
  }

  private void writeJoint(NiJoint joint) throws IOException {
    writeVec3(joint.positionRaw);
    if (joint.orientation == null) {
      writeVec3(null);
      writeVec3(null);
      writeVec3(null);
    } else {
      writeVec3(joint.orientation.getForward());
      writeVec3(joint.orientation.getUp());
      writeVec3(joint.orientation.getRight());
    }
  }

  @Override
  public void skeletonUpdated(NiSkeleton skeleton) {
    if (!active) return;

    try {
      for (NiJoint j : skeleton.getJoints())
        writeJoint(j);
      bw.write("\n");
    } catch (IOException e) {
    }
  }
}
