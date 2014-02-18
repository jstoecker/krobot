package edu.miami.cs.krobot.openni;

/**
 * Any object that wants to be notified when the primary OpenNI user skeleton has been updated.
 * 
 * @author justin
 */
public interface NiSkeletonSrcListener {
  
  void skeletonUpdated(NiSkeleton skeleton);
}
