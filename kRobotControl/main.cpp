#include <iostream>
#include <signal.h>
#include "naocontroller.h"
#include <unistd.h>
#include "motionplayer.h"
#include <alproxies/almotionproxy.h>

#define CYCLE_MS 10

bool running = true;

void terminate(int param)
{
  running = false;
}

int main(int argc, char* argv[])
{
  std::string naoIP = "127.0.0.1";
  int naoPort = 9559;
  int recvPort = 32771;
  bool balancing = false;
  std::string motionFile;

  int c;
  while ((c = getopt(argc, argv, "i:p:b:m:")) != -1)
  {
    switch (c)
    {
       case 'i': naoIP = optarg; break;
       case 'p': naoPort = atoi(optarg); break;
       case 'b': balancing = atoi(optarg); break;
       case 'm': motionFile = std::string(optarg); break;
    }
  }

  std::cout << "NAO address   : " << naoIP << std::endl;
  std::cout << "NAO port      : " << naoPort << std::endl;

  if (!motionFile.empty())
  {
    std::cout << "Motion file   : " << motionFile << std::endl;
    MotionPlayer player(motionFile.c_str());
    AL::ALMotionProxy motion(naoIP, naoPort);
    player.play(motion);
    return 0;
  }

  std::cout << "NAO balancing : " << balancing << std::endl;
  NaoController controller(naoIP, naoPort, recvPort, balancing);

  signal(SIGINT, terminate);

  std::cout << "Initializing robot for control..." << std::endl;
  controller.enable(true);
  std::cout << "Entered control loop (Ctrl-C to exit)" << std::endl;

  while (running)
  {
    controller.update();
    usleep(CYCLE_MS * 1000);
  }

  std::cout << "Shutting down..." << std::endl;
  controller.enable(false);
  std::cout << "Done." << std::endl;

  return 0;
}
