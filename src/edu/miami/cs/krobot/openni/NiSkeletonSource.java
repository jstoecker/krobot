package edu.miami.cs.krobot.openni;

import java.util.ArrayList;
import java.util.List;

/**
 * Any object that provides updates to an NiSkeleton. This would be any Kinect-like sensor or a
 * previously recorded file that updates a skeleton.
 * 
 * @author justin
 */
public abstract class NiSkeletonSource {

  protected List<NiSkeletonSrcListener> skeletonListeners = new ArrayList<NiSkeletonSrcListener>();

  public void addListener(NiSkeletonSrcListener l) {
    skeletonListeners.add(l);
  }

  public void removeListener(NiSkeletonSrcListener l) {
    skeletonListeners.remove(l);
  }
}
