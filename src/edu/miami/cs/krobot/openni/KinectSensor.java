package edu.miami.cs.krobot.openni;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.OpenNI.CalibrationProgressEventArgs;
import org.OpenNI.CalibrationProgressStatus;
import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.GeneralException;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.License;
import org.OpenNI.Point3D;
import org.OpenNI.PoseDetectionCapability;
import org.OpenNI.PoseDetectionEventArgs;
import org.OpenNI.SkeletonCapability;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointOrientation;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.SkeletonProfile;
import org.OpenNI.StatusException;
import org.OpenNI.UserEventArgs;
import org.OpenNI.UserGenerator;
import edu.miami.cs.js.math.vector.Orientation;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.Config;

public class KinectSensor extends NiSkeletonSource {

  public interface Listener {
    void kinectUpdated(KinectSensor sensor);
  }

  private Config                   config;
  private Context                  context;
  private DepthGenerator           depthGen;
  private UserGenerator            userGen;
  private UpdateThread             updateThread;
  private SkeletonCapability       skelCapability;
  private PoseDetectionCapability  poseCapability;
  private String                   calibrationPose;
  private Map<Integer, NiSkeleton> skeletons   = new HashMap<Integer, NiSkeleton>();
  private List<Listener>           listeners   = new CopyOnWriteArrayList<Listener>();
  private boolean                  initialized = false;
  private boolean                  active      = false;

  public DepthGenerator getDepthGenerator() {
    return depthGen;
  }

  public Collection<NiSkeleton> getSkeletons() {
    return Collections.unmodifiableCollection(skeletons.values());
  }

  public NiSkeleton getFirstSkeleton() {
    if (skeletons.size() > 0) return skeletons.values().iterator().next();
    return null;
  }

  public KinectSensor(Config config) {
    this.config = config;
  }

  public void addListener(Listener l) {
    listeners.add(l);
  }

  public void removeListener(Listener l) {
    listeners.remove(l);
  }
  
  public boolean isActive() {
    return active;
  }

  public boolean enable() {
    
    if (!initialized) {
      try {
        context = new Context();
        context.addLicense(new License("PrimeSense", "0KOIk2JeIBYClPWVnMoRKn5cdY4="));
        context.setGlobalMirror(true);

        depthGen = DepthGenerator.create(context);

        userGen = UserGenerator.create(context);
        poseCapability = userGen.getPoseDetectionCapability();
        skelCapability = userGen.getSkeletonCapability();
        calibrationPose = skelCapability.getSkeletonCalibrationPose();
        skelCapability.setSkeletonProfile(SkeletonProfile.ALL);
        userGen.getNewUserEvent().addObserver(new NewUserObserver());
        poseCapability.getPoseDetectedEvent().addObserver(new PoseDetectedObserver());
        skelCapability.getCalibrationCompleteEvent().addObserver(new CalibrationCompleteObserver());

        initialized = true;

      } catch (GeneralException e) {
        e.printStackTrace();
        initialized = false;
        return false;
      }
    }

    if (updateThread != null) updateThread.running = false;
    updateThread = new UpdateThread();
    updateThread.start();
    active = true;
    return true;
  }

  public void disable() {
    try {
      if (updateThread != null) updateThread.running = false;
      if (context != null) context.stopGeneratingAll();
      active = false;
    } catch (StatusException e) {
      e.printStackTrace();
    }
  }

  private class NewUserObserver implements IObserver<UserEventArgs> {
    public void update(IObservable<UserEventArgs> obs, UserEventArgs args) {
      try {
        poseCapability.startPoseDetection(calibrationPose, args.getId());
      } catch (StatusException e) {
        e.printStackTrace();
      }
    }
  }

  private class PoseDetectedObserver implements IObserver<PoseDetectionEventArgs> {
    public void update(IObservable<PoseDetectionEventArgs> obs, PoseDetectionEventArgs args) {
      int userID = args.getUser();
      try {
        poseCapability.stopPoseDetection(userID);
        skelCapability.requestSkeletonCalibration(userID, true);
      } catch (StatusException e) {
        e.printStackTrace();
      }
    }
  }

  private class CalibrationCompleteObserver implements IObserver<CalibrationProgressEventArgs> {
    public void update(IObservable<CalibrationProgressEventArgs> obs,
        CalibrationProgressEventArgs args) {
      try {
        int userID = args.getUser();
        if (args.getStatus() == CalibrationProgressStatus.OK) {
          createSkeleton(userID);
          skelCapability.startTracking(userID);
        } else {
          poseCapability.startPoseDetection(calibrationPose, userID);
        }
      } catch (StatusException e) {
        e.printStackTrace();
      }
    }
  }

  private class UpdateThread extends Thread {
    volatile boolean running = true;

    public void run() {
      try {
        context.startGeneratingAll();
      } catch (StatusException e1) {
        e1.printStackTrace();
      }

      while (running) {
        try {
          context.waitAnyUpdateAll();
          for (NiSkeleton skeleton : getSkeletons())
            updateSkeleton(skeleton);
          for (Listener l : listeners)
            l.kinectUpdated(KinectSensor.this);
        } catch (StatusException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void createSkeleton(int userId) {
    ArrayList<SkeletonJoint> joints = new ArrayList<SkeletonJoint>();
    for (SkeletonJoint jointId : SkeletonJoint.values()) {
      if (skelCapability.isJointAvailable(jointId)) {
        joints.add(jointId);
      }
    }

    NiSkeleton skeleton = new NiSkeleton(userId, joints, config);
    skeletons.put(userId, skeleton);
  }

  private void updateSkeleton(NiSkeleton skeleton) {
    for (NiJoint j : skeleton.joints)
      updateSkeletonJoint(j, skeleton);
    skeleton.updateOrientation();
    for (NiSkeletonSrcListener l : skeletonListeners)
      l.skeletonUpdated(skeleton);
  }

  private void updateSkeletonJoint(NiJoint joint, NiSkeleton skeleton) {
    try {
      if (skelCapability.isJointActive(joint.id) && skelCapability.isJointAvailable(joint.id)) {

        SkeletonJointPosition sjp = skelCapability.getSkeletonJointPosition(skeleton.id, joint.id);
        Point3D p = sjp.getPosition();
        joint.setPositionRaw((p == null) ? null : new Vec3(p.getX(), p.getY(), p.getZ()));

        SkeletonJointOrientation sjo = skelCapability.getSkeletonJointOrientation(skeleton.id,
            joint.id);
        if (sjo != null) {
          Vec3 x = new Vec3(sjo.getX1(), sjo.getX2(), sjo.getX3());
          Vec3 y = new Vec3(sjo.getY1(), sjo.getY2(), sjo.getY3());
          Vec3 z = new Vec3(sjo.getZ1(), sjo.getZ2(), sjo.getZ3());
          joint.orientation = new Orientation(z.times(-1), y, x);
        } else {
          joint.orientation = null;
        }
      }
    } catch (StatusException e) {
      e.printStackTrace();
    }
  }
}
