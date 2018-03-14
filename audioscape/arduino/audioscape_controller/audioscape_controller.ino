#include <NewPing.h>
#include <Bounce.h>
 
#define TRIGGER_PIN  3
#define ECHO_PIN     2
#define MAX_DISTANCE 200
 
NewPing sonar(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);


const int buttonPin = 4;
const int ledPin = 12;
Bounce pushbutton = Bounce(buttonPin, 10);  // 10 ms debounce

void setup() {
  pinMode(buttonPin, INPUT_PULLUP);
  pinMode(ledPin, OUTPUT);
}

boolean inside = false;
int trigger_distance = 40;
 
void loop() {
  if (pushbutton.update()) {
    if (pushbutton.fallingEdge()) {
      Keyboard.print("L");
    }else if (pushbutton.risingEdge()) {
      Keyboard.print("K");
    }
  }
//  delay(50);
  int distance = sonar.convert_cm(sonar.ping_median(5));
  if (distance<trigger_distance && !inside){
    inside=true;
    Keyboard.print("P");
    digitalWrite(ledPin, HIGH);
  }else if(distance>=trigger_distance && inside){
    inside=false;
    Keyboard.print("O");
    digitalWrite(ledPin, LOW);
  }

}
