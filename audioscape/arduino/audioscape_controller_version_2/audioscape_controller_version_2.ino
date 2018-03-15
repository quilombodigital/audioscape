#include <NewPing.h>
#include <Bounce.h>
 
#define TRIGGER_PIN  3
#define ECHO_PIN     2
#define MAX_DISTANCE 300
 
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

int reading = 0;
int distance = 0;
int average = 0;

void loop() {
  if (pushbutton.update()) {
    if (pushbutton.fallingEdge()) {
      Keyboard.print("L");
    }else if (pushbutton.risingEdge()) {
      Keyboard.print("K");
    }
  }

  if (reading==0){
    distance = sonar.convert_cm(sonar.ping_median(5));
    average = distance;
    reading=1;
  }else if (reading==1){
    distance = sonar.convert_cm(sonar.ping_median(5));
    average = (average+distance)/2;
    reading=2;
  }else if (reading==2){
    distance = sonar.convert_cm(sonar.ping_median(5));
    average = (average+distance)/2;
    reading=3;
  }else if (reading==3){
    reading=0;
    distance = sonar.convert_cm(sonar.ping_median(5));
    average = (average+distance)/2;
     if (average<trigger_distance && !inside){
        inside=true;
        Keyboard.print("P");
        digitalWrite(ledPin, HIGH);
        delay(500);
      }else if(average>=trigger_distance && inside){
        inside=false;
        Keyboard.print("O");
        digitalWrite(ledPin, LOW);
        delay(500);
      }
  }
 
}
