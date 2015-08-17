#include <TimerOne.h>

#include <SoftwareSerial.h>// import the serial library

#include <SPI.h>
#include "OliLedMatrixRGB.h"

#define _left '0'
#define _right '1'
#define _pause '2'
#define _continue '3'
#define _start '4'
#define _restart '5'
#define _shoot '6'
#define _up '7'
#define _down '8'

#define _player 'A'
#define _enemy 'B'
#define _bullet 'C'

/* LED MATRIX PINS */
#define LED_LATCH 5

//setup pins for led Matrix
OliLedMatrixRGB ledMatrix(LED_LATCH);

SoftwareSerial Bluetooth(9, 10); // RX, TX
int BluetoothData; // the data given from Android
int BluetoothData1;

byte movingCounter = 0;
byte spawningCounter = 0;
byte levelCounter = 0;

int score = 0;
byte level = 0;


struct Point {
  byte x = 0;
  byte y = 0;
};

Point enemies[8];
Point bullets[8];

Point player;

bool pause = false;

void startGame()
{
  pause = false;
  level = 0;
  score = 0;

  ledMatrix.clear();
  delay(1);
  ledMatrix.display();

  player.x = 2;
  player.y = 7;
  ledMatrix.setColor(Green);
  ledMatrix.display();
  ledMatrix.drawPixel(player.x, player.y);
  ledMatrix.display();



  for (int i = 0; i < 8; i++)
  {
    enemies[i].x = i;
    enemies[i].y = 254;
  }

  for (int i = 0; i < 8; i++)
  {
    bullets[i].x = i;
    bullets[i].y = 255;
  }

  //enemies[2].y = 0;
  //enemies[0].y = 0;
  //bullets[3].y = 6;
  //ledMatrix.drawPixel(enemies[2].x, enemies[2].y);
  //ledMatrix.drawPixel(enemies[0].x, enemies[0].y);
  //ledMatrix.drawPixel(bullets[3].x, bullets[3].y);
  makeEnemy(2, 0);
  makeEnemy(0, 0);
  makeBullet(2);
  ledMatrix.display();
}



void moveAllObjects()
{
  for (int i = 0; i < 8; i++)
  {
    if (enemies[i].y != 254)
      moveObject(enemies[i].x, enemies[i].y, _down, _enemy);
    if (bullets[i].y != 255)
      moveObject(bullets[i].x, bullets[i].y, _up, _bullet);
  }
}

void masterControl()
{
  if(pause)
    return;
  if (movingCounter >= 7 - level)
  {
      moveAllObjects();
      //      moveObject(enemies[0].x, enemies[0].y, _down, _enemy);
      //      moveObject(enemies[2].x, enemies[2].y, _down, _enemy);
      //      moveObject(bullets[2].x, bullets[2].y, _up, _bullet);

    movingCounter = 0;
  }
  if (spawningCounter >= 19 - 2 * level)
  {

      byte index = getNewEnemyPosition();
      Serial.println(index);
      if (index != 9)
        makeEnemy(enemies[index].x, 0);
    spawningCounter = 0;
  }
  if (levelCounter == 100)
  {
    if (level < 5)
      level++;
    levelCounter = 0;
  }
  levelCounter++;
  movingCounter++;
  spawningCounter++;
}


void setup() {
  // put your setup code here, to run once:
  Bluetooth.begin(9600);
  //Bluetooth.println("Bluetooth On please press 1 or 0 blink LED ..");
  Serial.begin(9600);
  //startGame();



  ledMatrix.clear();
  delay(1);
  ledMatrix.display();
  while (1)
  {
    if (Bluetooth.available())
    {
      Serial.println("Received");
      BluetoothData1 = Bluetooth.read();
      if (BluetoothData1 == _start)
        startGame();
      break;
    }
    delay(10);
  }
  Timer1.initialize(80000);
  Timer1.attachInterrupt(masterControl);
}

void makeEnemy(byte x, byte y) {
  if (enemies[x].y != 254)
    return;

  enemies[x].y = y;
  ledMatrix.setColor(Red);
  ledMatrix.drawPixel(x, y);
  ledMatrix.display();
}

void makeBullet(byte x)
{
  if (bullets[x].y != 255)
    return;

  bullets[x].y = 6;
  ledMatrix.setColor(White);
  ledMatrix.drawPixel(x, 6);
  ledMatrix.display();

}

void increaseScore()
{
  score = score + 10 * (level + 1);
  Bluetooth.print(String(score));
  Bluetooth.write("!");
}

bool checkCollision(byte &x, byte &y, char type) {
  bool collision = false;
  bool gameOver = false;
  if (type == _player) { //player
    if (7 == enemies[x].y) {
      ledMatrix.clearPixel(x, y);
      enemies[x].y = 254;
      collision = true;
      gameOver = true;
    }
  }
  else if (type == _enemy) { //enemy
    if (player.x == x && 7 == y) { //player collision
      ledMatrix.clearPixel(x, y);
      y = 254;
      collision = true;
      gameOver = true;
    }
    if (bullets[x].y == y) { //bullet collision
      ledMatrix.clearPixel(x, y);
      y = 254;
      bullets[x].y = 255;
      collision = true;
      increaseScore();
    }
  }
  else { //bullet
    if (enemies[x].y == y) {
      ledMatrix.clearPixel(x, y);
      y = 255;
      enemies[x].y = 254;
      collision = true;
      increaseScore();
    }
  }

  if (gameOver)
  {
    Bluetooth.write("G!");
    pause = true;
  }

  return collision;
}

bool resetObject(byte &x, byte &y, char type)
{
  ledMatrix.clearPixel(x, y);

  if (type == _bullet)
    bullets[x].y = 255;
  else
    enemies[x].y = 254;
}

byte getNewEnemyPosition()
{

  bool free = false;
  int minimalDistance = 15;
  int index = 0;
  for (int i = 0; i < 8; i++)
    if (enemies[i].y == 254)
    {
      int difference = enemies[i].x - player.x;
      if (difference < 0)
        difference *= -1;
      if (difference < minimalDistance)
      {
        minimalDistance = difference;
        index = i;
      }
      free = true;
    }
  if (free == false)
    return 9;

  return index;
}

void moveObject(byte &x, byte &y, char dir, char type) {
  int old_x = x;
  int old_y = y;

  if (dir == _left) { //left
    if (x == 0)
      return;
    else
      x--;
  }
  else if (dir == _right) { //right
    if (x == 7)
      return;
    else
      x++;
  }
  else if (dir == _up) { //up
    if (y == 0 || y > 253) {
      if (type == _bullet && y == 0)
        resetObject(x, y, type);

    }
    else
      y--;
  }
  else { //down
    if (y == 7 || y > 253)
    {
      if (type == _enemy && y == 7)
        resetObject(x, y, type);

    }
    else
      y++;
  }
  byte new_x = x;
  byte new_y = y;
  bool collision = checkCollision(x, y, type);

  ledMatrix.clearPixel(old_x, old_y);
  if (!collision)
  {
    if (type == _enemy)
      ledMatrix.setColor(Red);
    else if (type == _bullet)
      ledMatrix.setColor(White);
    else if (type == _player)
      ledMatrix.setColor(Green);

    ledMatrix.drawPixel(x, y);
  }
  else
  {
    ledMatrix.setColor(Yellow);
    ledMatrix.drawPixel(new_x, new_y);
    ledMatrix.display();
    Serial.print("Sad");
    Serial.print(new_x);
    Serial.print(" ");
    Serial.println(new_y);
    delay(20);
    ledMatrix.clearPixel(new_x, new_y);
  }
  ledMatrix.display();
}



void loop() {

  if (Bluetooth.available()) {
    Serial.println("Received");
    BluetoothData = Bluetooth.read();
    if (BluetoothData == _start)
      startGame();
    if (BluetoothData == _restart)
      startGame();
    if (BluetoothData == _pause)
      pause = true;
    if (BluetoothData == _continue)
      pause = false;
    if (!pause)
    {
      if (BluetoothData == _shoot)
        makeBullet(player.x);
      if (BluetoothData == _right) // if number 1 pressed ....
        moveObject(player.x, player.y, _right, _player);
      if (BluetoothData == _left) // if number 0 pressed ....
        moveObject(player.x, player.y, _left, _player);


    }
  }


  //delay(1000);// prepare for next data ...


}
