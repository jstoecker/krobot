#ifndef NAO_CONTROLLER_H
#define NAO_CONTROLLER_H

#include <alcommon/almodule.h>
#include <alproxies/almotionproxy.h>
#include <alproxies/alledsproxy.h>
#include <alproxies/altexttospeechproxy.h>
#include "networking/udpsocket.h"
#include "naostatus.h"


/**
  * @class Controller
  * @author Justin Stoecker (justin@cs.miami.edu)
  *
  * Receives status updates from kRobot and sends motion commands to a NAOqi.
  */
class NaoController
{
public:
  NaoController(std::string naoAddr, int naoPort, int recvPort, bool balancing);
  ~NaoController();
  void enable(bool enabled);
  void update();

private:
  AL::ALMotionProxy       motion;
  AL::ALLedsProxy         leds;
  AL::ALTextToSpeechProxy tts;
  net::UDPSocket          socket;
  bool                    balancing;

  /** Receive updates from kRobot into a current status */
  bool receive(NaoStatus& status);
};

#endif // NAO_CONTROLLER_H
