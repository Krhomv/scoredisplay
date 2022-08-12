
#include "ble_manager.h"

void setup()
{
  Serial.begin(9600);
  
  BLEManager::begin();
}

void loop()
{
  BLEManager::update();

  int team1Score = BLEManager::getTeam1Score();
  // ...

}