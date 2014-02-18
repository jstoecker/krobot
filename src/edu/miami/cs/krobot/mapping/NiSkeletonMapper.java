package edu.miami.cs.krobot.mapping;

import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.openni.NiSkeleton;
import edu.miami.cs.krobot.openni.NiSkeletonSrcListener;

/**
 * Maps an OpenNI skeleton to a robot model.
 * 
 * @author justin
 */
public abstract class NiSkeletonMapper implements NiSkeletonSrcListener {

  protected Model model;

  public void initialize(Model model) {
    this.model = model;
  }

  @Override
  public void skeletonUpdated(NiSkeleton skeleton) {
    model.setSkeleton(skeleton);
    map(skeleton);
    model.commitUpdate();
  }

  public abstract void map(NiSkeleton skeleton);

  /**
   * Empty mapper used if no mapper is provided by the model.
   */
  public static class DummyMapper extends NiSkeletonMapper {
    @Override
    public void map(NiSkeleton skeleton) {
    }
  }
}