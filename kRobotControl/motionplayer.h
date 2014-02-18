#ifndef MOTION_PLAYER_H
#define MOTION_PLAYER_H

#include <string>
#include <vector>
#include <alproxies/almotionproxy.h>
#include "naostatus.h"

/**
  * @class MotionPlayer
  *
  * Plays a recorded motion file in one smooth animation.
  */
class MotionPlayer
{
public:
  MotionPlayer(const char* filename);

  void play(AL::ALMotionProxy& motion);

private:
  AL::ALValue angles;
  AL::ALValue times;
};

#endif
