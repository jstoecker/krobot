#ifndef NAO_STATUS_H
#define NAO_STATUS_H

#include <alvalue/alvalue.h>

class NaoStatus
{
public:
  NaoStatus();
  ~NaoStatus();

  static const AL::ALValue head;
  static const AL::ALValue leftArm;
  static const AL::ALValue rightArm;
  static const AL::ALValue arms;
  static const AL::ALValue leftLeg;
  static const AL::ALValue rightLeg;
  static const AL::ALValue legs;
  static const AL::ALValue body;
  static const AL::ALValue upperBody;

  AL::ALValue headAngles() const;
  AL::ALValue leftArmAngles() const;
  AL::ALValue rightArmAngles() const;
  AL::ALValue armAngles() const;
  AL::ALValue leftLegAngles() const;
  AL::ALValue rightLegAngles() const;
  AL::ALValue legAngles() const;
  AL::ALValue bodyAngles() const;
  AL::ALValue upperBodyAngles() const;

  void update(std::string message);
  
private:
  float angles[22];
};

#endif
