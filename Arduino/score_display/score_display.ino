#include "score_display_bt_sync.h"

ScoreDisplayBTSync BTSync(Serial1, Serial);

void setup() {
  // initialize the digital pins as outputs.
  Serial.begin(9600);
  Serial1.begin(9600);
}

void loop() 
{
  BTSync.update();

  int team1Score = BTSync.getTeam1Score();
  int team1Red, team1Green, team1Blue;

  BTSync.getTeam1Color(team1Red, team1Green, team1Blue);

  // ... 
}

