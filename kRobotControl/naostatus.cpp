#include "naostatus.h"
#include <sstream>

static const char* names[] = {
  "HeadYaw",
  "HeadPitch",
  "LShoulderPitch",
  "LShoulderRoll",
  "LElbowYaw",
  "LElbowRoll",
  "RShoulderPitch",
  "RShoulderRoll",
  "RElbowYaw",
  "RElbowRoll",
  "LHipYawPitch",
  "LHipRoll", 
  "LHipPitch",
  "LKneePitch",
  "LAnklePitch",
  "LAnkleRoll",
  "RHipYawPitch",
  "RHipRoll", 
  "RHipPitch",
  "RKneePitch",
  "RAnklePitch",
  "RAnkleRoll"
};

const AL::ALValue NaoStatus::head      = AL::ALValue(names, 2);
const AL::ALValue NaoStatus::leftArm   = AL::ALValue(names + 2, 4);
const AL::ALValue NaoStatus::rightArm  = AL::ALValue(names + 6, 4);
const AL::ALValue NaoStatus::arms      = AL::ALValue(names + 2, 8);
const AL::ALValue NaoStatus::leftLeg   = AL::ALValue(names + 10, 6);
const AL::ALValue NaoStatus::rightLeg  = AL::ALValue(names + 16, 6);
const AL::ALValue NaoStatus::legs      = AL::ALValue(names + 10, 12);
const AL::ALValue NaoStatus::body      = AL::ALValue(names, 22);
const AL::ALValue NaoStatus::upperBody = AL::ALValue(names, 10);

NaoStatus::NaoStatus()
{
}

NaoStatus::~NaoStatus()
{
}

inline AL::ALValue alAngleValues(const float* angles, int numAngles)
{
  // the constructor ALValue(const float* pArrayOfFloats, int nNbrElements)
  // does not work and throws an exception, so this is necessary for now...
  AL::ALValue alAngles;
  alAngles.arraySetSize(numAngles);
  for (int i = 0; i < numAngles; i++)
    alAngles[i] = (*angles++);
  return alAngles;
}

AL::ALValue NaoStatus::headAngles() const
{
  return alAngleValues(angles, 2);
}

AL::ALValue NaoStatus::leftArmAngles() const
{
  return alAngleValues(angles + 2, 4);
}

AL::ALValue NaoStatus::rightArmAngles() const
{
  return alAngleValues(angles + 6, 4);
}

AL::ALValue NaoStatus::armAngles() const
{
  return alAngleValues(angles + 2, 8);
}

AL::ALValue NaoStatus::leftLegAngles() const
{
  return alAngleValues(angles + 10, 6);
}

AL::ALValue NaoStatus::rightLegAngles() const
{
  return alAngleValues(angles + 16, 6);
}

AL::ALValue NaoStatus::legAngles() const
{
  return alAngleValues(angles + 10, 12);
}

AL::ALValue NaoStatus::bodyAngles() const
{
  return alAngleValues(angles, 22);
}

AL::ALValue NaoStatus::upperBodyAngles() const
{
  return alAngleValues(angles, 10);
}

void NaoStatus::update(std::string message)
{
  std::stringstream ss(message);
  std::string token;
  for (int i = 0; i < 22; i++)
  {
    ss >> token;
    angles[i] = atof(token.c_str());
  }
}
