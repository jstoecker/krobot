#include <iostream>
#include "naocontroller.h"
#include "naostatus.h"
#include "naoposes.h"
#include "motionplayer.h"

NaoController::NaoController(std::string naoAddr, int naoPort, int recvPort,
                             bool balancing)
  : motion(naoAddr, naoPort),
    leds(naoAddr, naoPort),
    tts(naoAddr, naoPort),
    balancing(balancing)
{
  socket.setNonBlocking(true);
  socket.bind(net::Addr(recvPort, net::Addr::ANY));
}

NaoController::~NaoController()
{
}

void NaoController::enable(bool enabled)
{
  leds.fadeRGB("AllLeds", 0x00000000, 0.3f);
  if (enabled)
  {
    leds.fadeRGB("FaceLeds", 0x00FFFF00, 0.5f);
    motion.stiffnessInterpolation("Body", 1.0f, 1.0f);
    naoInit(motion, 0.2f);
    if (!balancing)
      naoStand(motion, 0.2f);
    leds.fadeRGB("FaceLeds", 0x00FF0000, 0.3f);
    tts.say("Ready");
  }
  else
  {
    leds.fadeRGB("FaceLeds", 0x00FFFF00, 0.5f);
    naoInit(motion, 0.2f);
    naoSit(motion, 0.1f);
    motion.stiffnessInterpolation("Body", 0.0f, 2.0f);
    leds.fadeRGB("FaceLeds", 0x000000FF, 0.3f);
    tts.say("End");
  }
}

void NaoController::update()
{
  if (balancing)
  {
    // idea: collect all packets for a 2 second window, place them in a list
    // by time, and then play in a single motion (repeated motion calls is
    // not nearly as smooth)
    // need a second thread that does the collecting
    NaoStatus status;
    if (receive(status))
    {
      motion.wbEnable(true);
      motion.wbFootState("Fixed", "Legs");
      motion.wbEnableBalanceConstraint(true, "Legs");
      //motion.angleInterpolationWithSpeed("Body", targetAngles, 0.2f); 
      motion.wbEnable(false);
    }
  }
  else
  {
    NaoStatus status;
    if (receive(status))
      motion.setAngles(NaoStatus::upperBody, status.upperBodyAngles(), 0.2);
  }
}

bool NaoController::receive(NaoStatus& status)
{
  char buf[255];
  int received;

  if ((received = socket.recv(buf, 255)) > 0)
  {
    std::string message;
    message.assign(buf, received);
    status.update(message);
    return true;
  }

  return false;
}
