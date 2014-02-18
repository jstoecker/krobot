#include "naoposes.h"
#include <iostream>
#include <alerror/alerror.h>

static const float initAngles[] = {
   0.000000, // HeadYaw
   0.000000, // HeadPitch
   1.395560, // LShoulderPitch
   0.348889, // LShoulderRoll
  -1.395560, // LElbowYaw
  -1.046670, // LElbowRoll
   0.000000, // LHipYawPitch
   0.000000, // LHipRoll
  -0.348889, // LHipPitch
   0.697778, // LKneePitch
  -0.348889, // LAnklePitch
   0.000000, // LAnkleRoll
   0.000000, // RHipYawPitch
   0.000000, // RHipRoll
  -0.348889, // RHipPitch
   0.697778, // RKneePitch
  -0.348889, // RAnklePitch
   0.000000, // RAnkleRoll
   1.395560, // RShoulderPitch
  -0.348889, // RShoulderRoll
   1.395560, // RElbowYaw
   1.046670, // RElbowRoll
};

static const float standAngles[] = {
  -0.004644, // HeadYaw
  -0.187190, // HeadPitch
   1.592250, // LShoulderPitch
   0.165630, // LShoulderRoll
  -1.227243, // LElbowYaw
  -0.587480, // LElbowRoll
  -0.161028, // LHipYawPitch
   0.112024, // LHipRoll
   0.204064, // LHipPitch
  -0.090548, // LKneePitch
   0.070522, // LAnklePitch
  -0.107338, // LAnkleRoll
  -0.161028, // RHipYawPitch
  -0.067454, // RHipRoll
   0.188640, // RHipPitch
  -0.073590, // RKneePitch
   0.062936, // RAnklePitch
   0.066004, // RAnkleRoll
   1.486488, // RShoulderPitch
  -0.112024, // RShoulderRoll
   1.181138, // RElbowYaw
   0.435699, // RElbowRoll
};

static const float sitAngles[] = {
   0.000000, // HeadYaw
   0.000000, // HeadPitch
   1.395560, // LShoulderPitch
   0.348889, // LShoulderRoll
  -1.395560, // LElbowYaw
  -1.046670, // LElbowRoll
   0.000000, // LHipYawPitch
   0.000000, // LHipRoll
  -1.090000, // LHipPitch
   2.110000, // LKneePitch
  -1.070000, // LAnklePitch
   0.000000, // LAnkleRoll
   0.000000, // RHipYawPitch
   0.000000, // RHipRoll
  -1.090000, // RHipPitch
   2.110000, // RKneePitch
  -1.070000, // RAnklePitch
   0.000000, // RAnkleRoll
   1.395560, // RShoulderPitch
  -0.348889, // RShoulderRoll
   1.395560, // RElbowYaw
   1.046670, // RElbowRoll
};

void setPose(AL::ALMotionProxy& motion, const float* angles, float speed)
{
  AL::ALValue values;
  values.arraySetSize(22);
  for (int i = 0; i < 22; i++) values[i] = (*angles++);
  motion.angleInterpolationWithSpeed("Body", values, speed);
}

void naoSit(AL::ALMotionProxy& motion, float speed)
{
  setPose(motion, sitAngles, speed);
}

void naoInit(AL::ALMotionProxy& motion, float speed)
{
  setPose(motion, initAngles, speed);
}

void naoStand(AL::ALMotionProxy& motion, float speed)
{
  setPose(motion, standAngles, speed);
}
