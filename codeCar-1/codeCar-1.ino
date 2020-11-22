#include <iarduino_Encoder_tmr.h>
#include<iarduino_Bluetooth_HC05.h>
#include<SoftwareSerial.h>
//  Подключаем библиотеку iarduino_Encoder_tmr для работы с энкодерами через аппаратный таймер
iarduino_Encoder_tmr enc(11, 12);             //  Объявляем объект enc для работы с энкодером указывая (№ вывода A, № вывода B)
//  Если при объявлении объектов перепутать выводы, то поворот влево будет расценен как поворот вправо и наоборот
unsigned long timing1; // esp
 String my_speed, my_time;
int Come;
int a ;
int index1, index2, index3;
int Come1;
int Speed1;
int Way1;
int blut;
int Speed = 255;
int Time;
int Time1;
// освобождаем память в контроллере для переменой
int    i = 0;
// Двигатель A
int enA = 9;
int in1 = 7;
int in2 = 6;
// Двигатель B
int enB = 10;
int in3 = 5;
int in4 = 4;
String val;
boolean readSpeed = false;
boolean readTime = false;
bool is_working_motors = false;

//SoftwareSerial blut(2,3);

//  Определяем переменную для подсчёта дискретных поворотов энкодера
void setup()
{
  Serial.begin(9600);                       //  Инициируем передачу данных в монитор последовательного порта
  enc.begin();                              //  Инициируем работу с энкодером




  // инициализируем все пины для управления двигателями как outputs

  pinMode(enA, OUTPUT);

  pinMode(enB, OUTPUT);

  pinMode(in1, OUTPUT);

  pinMode(in2, OUTPUT);

  pinMode(in3, OUTPUT);

  pinMode(in4, OUTPUT);

}

void loop() {
 // int a = enc.read();                       //  Читаем состояние энкодера в переменную a
 // if (a) {                                  //  Если энкодер зафиксировал поворот, то ...
    //i = i + a;   /* i+=a*/                //  Меняем значение переменной i на 1, т.к. в переменной a находится -1 (при повороте влево), или +1 (при повороте вправо).
   // Serial.println(i);                    //  Выводим значение переменной i
  
 // while (Serial.available()> 0 ) // проверяем, поступают ли какие-то команды
 // {
   //  a = blut.readString(); 

    //a = Serial.read(); // переменная Come равна полученной команде
   // Time = (Time1 - 48)*1000;
 
//Serial.println(Come);
//Serial.println("Pervichnoe");
//Serial.println(Come1);
//Serial.println("Perevedennoe");
/*
//if (a == '@') {
 //// val = "";
      //указываем, что сейчас считаем скорость
      readSpeed = true;
} else if (readSpeed) {
  //если пора считывать скорость и байт не равен решетке
      //добавляем байт к val
      if(a == '#') {
        //если байт равен решетке, данные о скорости кончились
        //выводим в монитор порта для отладки
        Serial.println(val);
        //указываем, что скорость больше не считываем
        readSpeed = false;
        //передаем полученную скорость в функцию езды 
        val = Speed;
        //обнуляем val
        val = "";
        //выходим из цикла, чтобы считать следующий байт
        return;
}else if (a == '#') {
      //начинаем считывать угол поворота
      readTime = true; 
    } else if (readTime) {
      //если решетка, то заканчиваем считывать угол
      //пока не решетка, добавляем значение к val
      if(a == '#') {
       Serial.println(val);
       Serial.println("-----");
        readTime = false;
        //передаем значение в функцию поворота
        val = Time;
        val= "";
        return;

 //if ((Come > 0)and(Come < 30)) {
  //int Time = Come1;
 // Serial.println(Time);
 // Serial.println("Vremya");
 //
 }

 */
 if (Serial.available() > 0) {                 //если есть доступные данные c блютуз 
    String incomingByte = Serial.readString();  // считываем байт
    index1 = incomingByte.indexOf('/');
    index2 = incomingByte.indexOf('/', index1 +1);
    index3 = incomingByte.indexOf('/', index2 +1);
    my_speed   = incomingByte.substring(1,index2);
    my_time  = incomingByte.substring(index2+1,index3);
    Speed = round(my_speed.toInt() * 5.1);
    Time = my_time.toInt() * 1000;
    Serial.println(my_speed + " " + my_time);
    Serial.print("start");
    timing1 = millis(); 
    // включение моторов
    analogWrite(enA, Speed);
    analogWrite(enB, Speed);
    digitalWrite(in1, HIGH);
    digitalWrite(in3, HIGH);
    digitalWrite(in2, LOW);
    digitalWrite(in4, LOW);
    // моторы включены
    is_working_motors = true;
    
 }
 
    
  if ((is_working_motors) && (millis() - timing1 > Time)){
      timing1 = millis(); 
      is_working_motors = false;
      // выключаем моторы
      digitalWrite(in2, LOW);
      digitalWrite(in4, LOW);
      //millis(Time);
      digitalWrite(in1, LOW);
      digitalWrite(in2, LOW);
      
      digitalWrite(in3, LOW);
      digitalWrite(in4, LOW);
      Serial.print("stop");


      // выключили
/*
 while ((Time > 0)and(Speed > 0)) {
    analogWrite(enA, Speed);
   analogWrite(enB, Speed);
    
   digitalWrite(in1, HIGH);
   digitalWrite(in3, HIGH);
   //millis(Time);
     digitalWrite(in2, LOW);
     digitalWrite(in4, LOW);

    

     digitalWrite(in1, HIGH);
    digitalWrite(in2, HIGH);
     digitalWrite(in3, HIGH);
     digitalWrite(in4, HIGH);
     }*/
  }
}



    
 
