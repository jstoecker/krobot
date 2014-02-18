#include "motionplayer.h"
#include "naoposes.h"
#include <vector>
#include <fstream>
#include <cstdio>
#include <iostream>
#include <alerror/alerror.h>
#include <iostream>
#include <cmath>

MotionPlayer::MotionPlayer(const char* filename)
{
  for (int i = 0; i < 22; i++)
  {
    angles.arrayPush(std::vector<float>());
    times.arrayPush(std::vector<float>());
  }

  float time = 3.0;
  std::ifstream motionFile(filename); 
  std::string line;

  float prevAngles[22];
  for (int i = 0; i < 22; i++)
    prevAngles[i] = 0;
  
  if (motionFile.is_open())
  {
    while (motionFile.good())
    {
      getline(motionFile, line);

      if (line.size() > 0)
      {
        NaoStatus frame;
        frame.update(line);

        float maxChange = 0;

        AL::ALValue bodyAngles = frame.bodyAngles();
        for (int i = 0; i < 22; i++)
        {
          float curAngle = bodyAngles[i]; 
          angles[i].arrayPush(curAngle);
          times[i].arrayPush(time);

          float change = fabs(prevAngles[i] - curAngle);

          if (change > maxChange)
            maxChange = change;

          prevAngles[i] = curAngle;
        }

        // adapt time increment if maxChange is too large?
        // IMPORTANT: if filtering / clamping is disable in the mapper the
        // large angle changes will cause max body velocity to be violated
        time += 0.033;
      }
    }
    motionFile.close();
  }
}

void MotionPlayer::play(AL::ALMotionProxy& motion)
{
  try
  {
    motion.stiffnessInterpolation(NaoStatus::body, 1.0f, 1.0f);
    naoInit(motion, 0.2f);
    motion.angleInterpolation(NaoStatus::body, angles, times, true); 
    naoInit(motion, 0.2f);
    naoSit(motion, 0.1f);
    motion.stiffnessInterpolation(NaoStatus::body, 0.0f, 1.0f);
  }
  catch (const AL::ALError& e)
  {
    std::cout << e.what() << std::endl;
  }
}

