#ifndef NAO_POSES_H
#define NAO_POSES_H

#include <alproxies/almotionproxy.h>

void setPose(AL::ALMotionProxy& motion, const float* angles, float speed);

void naoSit(AL::ALMotionProxy& motion, float speed);

void naoStand(AL::ALMotionProxy& motion, float speed);

void naoInit(AL::ALMotionProxy& motion, float speed);

#endif
