cmake_minimum_required(VERSION 2.8)
project(kRobotControl)

find_package(qibuild)

set(_srcs
  main.cpp
  naocontroller.h
  naocontroller.cpp
  naostatus.h
  naostatus.cpp
  naoposes.h
  naoposes.cpp
  motionplayer.h
  motionplayer.cpp
  networking/addr.h
  networking/addr.cpp
  networking/netexception.cpp
  networking/netexception.h
  networking/socket.h
  networking/socket.cpp
  networking/udpsocket.h
  networking/udpsocket.cpp)

qi_create_bin(kRobotControl ${_srcs})

qi_use_lib(kRobotControl ALCOMMON)
