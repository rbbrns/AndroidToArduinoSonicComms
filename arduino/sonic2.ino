#include <toneAC.h>
#include <avr/pgmspace.h>


const int OUT_TONES[] = {
  //16700, 18100, 17400 };
  20231, 21231 };
const int IN_TONES[] = {
  //16700, 18100, 17400 };
  19231, 20765 };

boolean ready = 0;
//sizeof(IN_TONES)/sizeof(int);
#define ADC_CENTER 127
#define WINDOW_SIZE 128
#define BIT_PERIOD 8
#define BIT_PERIOD_2 16
#define BIT_PERIOD_5_2 20
#define BIT_PERIOD_3_2 12
#define BIT_PERIOD_1_2 4
#define MSG_LENGTH 3
#define NUM_BYTES 5
#define NUM_MSGS 5
#define THRESHOLD 25000

const unsigned int ON_THRESHOLD = 50000;
const unsigned int OFF_THRESHOLD = ON_THRESHOLD/2;

enum {
  IDLE,START,HIGH_PULSE,LOW_PULSE,DONE} 
state = IDLE;

const float SAMPLE_RATE = 76923.0769;//38461.5385 //8928.57143
const float STAMP_MS = (float)WINDOW_SIZE/(SAMPLE_RATE/1000);

byte bytei = 0;
byte biti = 0;
byte lastSmpl = 0;
byte bankIn = 0;
byte bankProc = 0;

unsigned short samplei=0;
unsigned long stamp=0;
unsigned long lastStamp=0;

struct msg_t {
  byte bytes[NUM_BYTES];
  boolean ready;
  byte len;
};

struct msg_t msgs[NUM_MSGS];

int Q1,Q2;

static const uint8_t PROGMEM crc8_table[] = {
  0, 94,188,226, 97, 63,221,131,194,156,126, 32,163,253, 31, 65,
  157,195, 33,127,252,162, 64, 30, 95,  1,227,189, 62, 96,130,220,
  35,125,159,193, 66, 28,254,160,225,191, 93,  3,128,222, 60, 98,
  190,224,  2, 92,223,129, 99, 61,124, 34,192,158, 29, 67,161,255,
  70, 24,250,164, 39,121,155,197,132,218, 56,102,229,187, 89,  7,
  219,133,103, 57,186,228,  6, 88, 25, 71,165,251,120, 38,196,154,
  101, 59,217,135,  4, 90,184,230,167,249, 27, 69,198,152,122, 36,
  248,166, 68, 26,153,199, 37,123, 58,100,134,216, 91,  5,231,185,
  140,210, 48,110,237,179, 81, 15, 78, 16,242,172, 47,113,147,205,
  17, 79,173,243,112, 46,204,146,211,141,111, 49,178,236, 14, 80,
  175,241, 19, 77,206,144,114, 44,109, 51,209,143, 12, 82,176,238,
  50,108,142,208, 83, 13,239,177,240,174, 76, 18,145,207, 45,115,
  202,148,118, 40,171,245, 23, 73,  8, 86,180,234,105, 55,213,139,
  87,  9,235,181, 54,104,138,212,149,203, 41,119,244,170, 72, 22,
  233,183, 85, 11,136,214, 52,106, 43,117,151,201, 74, 20,246,168,
  116, 42,200,150, 21, 75,169,247,182,232, 10, 84,215,137,107, 53};

static const uint8_t PROGMEM crc4_table[] = {
  5, 11, 25, 23, 26, 20, 6, 8, 28, 18, 0, 14, 3, 13, 31, 17, 16, 30, 12, 2, 15, 1, 19, 29, 9, 7, 21, 27, 22, 24, 10, 4, 8, 6, 20, 26, 23, 25, 11, 5, 17, 31, 13,
  3, 14, 0, 18, 28, 29, 19, 1, 15, 2, 12, 30, 16, 4, 10, 24, 22, 27, 21, 7, 9, 31, 17, 3, 13, 0, 14, 28, 18, 6, 8, 26, 20, 25, 23, 5, 11, 10, 4, 22, 24, 21, 27, 9
    , 7, 19, 29, 15, 1, 12, 2, 16, 30, 18, 28, 14, 0, 13, 3, 17, 31, 11, 5, 23, 25, 20, 26, 8, 6, 7, 9, 27, 21, 24, 22, 4, 10, 30, 16, 2, 12, 1, 15, 29, 19, 22, 24,
  10, 4, 9, 7, 21, 27, 15, 1, 19, 29, 16, 30, 12, 2, 3, 13, 31, 17, 28, 18, 0, 14, 26, 20, 6, 8, 5, 11, 25, 23, 27, 21, 7, 9, 4, 10, 24, 22, 2, 12, 30, 16, 29, 1,
  9, 1, 15, 14, 0, 18, 28, 17, 31, 13, 3, 23, 25, 11, 5, 8, 6, 20, 26, 12, 2, 16, 30, 19, 29, 15, 1, 21, 27, 9, 7, 10, 4, 22, 24, 25, 23, 5, 11, 6, 8, 26, 20, 0,
  14, 28, 18, 31, 17, 3, 13, 1, 15, 29, 19, 30, 16, 2, 12, 24, 22, 4, 10, 7, 9, 27, 21, 20, 26, 8, 6, 11, 5, 23, 25, 13, 3, 17, 31, 18, 28, 14, 0};

uint8_t crc8(const uint8_t *addr, uint8_t len)
{
  uint8_t crc = 0xff;
  while (len--) {
    crc = pgm_read_byte(crc8_table + (crc ^ *addr++));
  }
  return crc;
}

/* table of Hamming codes hammingCodes[x] is the x encoded */
static const uint8_t PROGMEM hammingEncode[16] =
{
  0x00,   /* 0 */
  0x71,   /* 1 */
  0x62,   /* 2 */
  0x13,   /* 3 */
  0x54,   /* 4 */
  0x25,   /* 5 */
  0x36,   /* 6 */
  0x47,   /* 7 */
  0x38,   /* 8 */
  0x49,   /* 9 */
  0x5A,   /* A */
  0x2B,   /* B */
  0x6C,   /* C */
  0x1D,   /* D */
  0x0E,   /* E */
  0x7F    /* F */
};

/* table convering encoded value (with error) to original data */
/* hammingDecodeValues[code] = original data */
static const uint8_t PROGMEM hammingDecode[128] =
{
  0x00, 0x00, 0x00, 0x03, 0x00, 0x05, 0x0E, 0x07,     /* 0x00 to 0x07 */
  0x00, 0x09, 0x0E, 0x0B, 0x0E, 0x0D, 0x0E, 0x0E,     /* 0x08 to 0x0F */
  0x00, 0x03, 0x03, 0x03, 0x04, 0x0D, 0x06, 0x03,     /* 0x10 to 0x17 */
  0x08, 0x0D, 0x0A, 0x03, 0x0D, 0x0D, 0x0E, 0x0D,     /* 0x18 to 0x1F */
  0x00, 0x05, 0x02, 0x0B, 0x05, 0x05, 0x06, 0x05,     /* 0x20 to 0x27 */
  0x08, 0x0B, 0x0B, 0x0B, 0x0C, 0x05, 0x0E, 0x0B,     /* 0x28 to 0x2F */
  0x08, 0x01, 0x06, 0x03, 0x06, 0x05, 0x06, 0x06,     /* 0x30 to 0x37 */
  0x08, 0x08, 0x08, 0x0B, 0x08, 0x0D, 0x06, 0x0F,     /* 0x38 to 0x3F */
  0x00, 0x09, 0x02, 0x07, 0x04, 0x07, 0x07, 0x07,     /* 0x40 to 0x47 */
  0x09, 0x09, 0x0A, 0x09, 0x0C, 0x09, 0x0E, 0x07,     /* 0x48 to 0x4F */
  0x04, 0x01, 0x0A, 0x03, 0x04, 0x04, 0x04, 0x07,     /* 0x50 to 0x57 */
  0x0A, 0x09, 0x0A, 0x0A, 0x04, 0x0D, 0x0A, 0x0F,     /* 0x58 to 0x5F */
  0x02, 0x01, 0x02, 0x02, 0x0C, 0x05, 0x02, 0x07,     /* 0x60 to 0x67 */
  0x0C, 0x09, 0x02, 0x0B, 0x0C, 0x0C, 0x0C, 0x0F,     /* 0x68 to 0x6F */
  0x01, 0x01, 0x02, 0x01, 0x04, 0x01, 0x06, 0x0F,     /* 0x70 to 0x77 */
  0x08, 0x01, 0x0A, 0x0F, 0x0C, 0x0F, 0x0F, 0x0F      /* 0x78 to 0x7F */
};

boolean processBytes();

void setup() {
  Serial.begin(115200);
  Serial.println("Setup");
  Serial.println(STAMP_MS);
  int x = -10;
  Serial.println(x>>2);
  x = 10;
  Serial.println(x>>2);
  Serial.println(sizeof(byte));
  Serial.println(sizeof(short));
  Serial.println(sizeof(int));
  Serial.println(sizeof(long));  
  initADC();

  /*for(int i=0; i<1000j; i++) {
   for(int f=0; f<10; f++) {
   toneAC(OUT_TONES[0] + ((OUT_TONES[1]-OUT_TONES[0])*f)/10);
   delay(1);
   }
   for(int f=0; f<10; f++) {
   toneAC(OUT_TONES[1] - ((OUT_TONES[1]-OUT_TONES[0])*f)/10);
   delay(1);
   }
   }*/


  byte msg[] = {
    (byte)0x00, (byte)0xff, (byte) 0x55};
  while(0){
    FMSimpleSend(msg,sizeof(msg));
    delay(2000);
  }
}

int tcount = 0;
void loop()
{
  if(bankProc != bankIn) {
    if(msgs[bankProc].ready) {
      //if(validate(msgs[bankProc])) {
        
      //}
      if(msgs[bankProc].len>=1) {
        msgs[bankProc].bytes[0] ^= 0x20;
        delay(200);
        FMSimpleSend(msgs[bankProc].bytes,1);
      }
      bankProc = (bankProc+1)%NUM_MSGS;
    }
    msgs[bankProc].ready = false;
  }
}
enum TYPE {
  RESERVED_TYPE,LIGHT1,LIGHT2,ALPHA} 
;
enum ACTION {
  RESERVED_ACTION,ON,OFF,TOGGLE,SET,GET} 
;

boolean validate(struct msg_t msg) {
  if(msg.len <= 1) return false;
  if(crc8(msg.bytes,msg.len-1) == msg.bytes[msg.len-1]) return true;
  else return false;
}

#if 0
boolean processBytes(struct msg_t msg) {
  
  TYPE type = (TYPE)(msg.bytes[0]>>4);
  ACTION action = (ACTION)(msg.bytes[0]&0xF);
  
  switch(action) {
  case ON:
    break;
  case OFF:

    break;
  case TOGGLE:
    if(type==ALPHA && msg.len == 3) {
      msg.bytes[1] ^= 0x20;
      delay(200);
      FMSimpleSend(msg.bytes[1] ^ 0x20,1);
    }
    break;
  case SET:
    break;
  case GET:
    break;
  case TEST:
    break;
    /*case TOGGLE_RELAY2:
     case ON_RELAY1:
     case ON_RELAY2:
     case OFF_RELAY1:
     case OFF_RELAY2:
     case SET_RELAY1:
     case SET_RELAY2:
     case GET_RELAY1:
     case GET_RELAY2:
     
     case GET_TEMP:
     case GET_HUMID:
     case GET_GAS:
     case GET_CURRENT:
     
     case SET_TEMP:
     case SET_HUMID:
     case SET_GAS:
     case SET_*/

  }
  bytej = (bytej+1)%BYTES_LEN;
}
#endif

void initADC() {
  cli();//diable interrupts

  //set up continuous sampling of analog pin 0

  //clear ADCSRA and ADCSRB registers
  ADCSRA = 0;
  ADCSRB = 0;

  ADMUX |= (1 << REFS0); //set reference voltage
  ADMUX |= (1 << ADLAR); //left align the ADC value- so we can read highest 8 bits from ADCH register only

  ADCSRA |= (1 << ADPS2) ;//| (1 << ADPS0); //set ADC clock with 32 prescaler- 16mHz/32=500kHz
  ADCSRA |= (1 << ADATE); //enabble auto trigger
  enableADCInterrupt();//ADCSRA |= (1 << ADIE); //enable interrupts when measurement complete
  ADCSRA |= (1 << ADEN); //enable ADC
  ADCSRA |= (1 << ADSC); //start ADC measurements

  sei();//enable interrupts
}

void enableADCInterrupt() {
  ADCSRA |= (1 << ADIE); //enable interrupts when measurement complete
}
void disableADCInterrupt() {
  ADCSRA &= ~(1 << ADIE); //enable interrupts when measurement complete
}


ISR(ADC_vect) {
  int Q0 = -Q2 + ((int)ADCH-ADC_CENTER);
  Q2 = Q1;
  Q1 = Q0;

  samplei++;
  if(samplei == WINDOW_SIZE) { 
    samplei = 0;
    unsigned long mag = (unsigned long)((long)Q1)*Q1+(unsigned long)((long)Q2)*Q2;
    Q1=Q2=0;

    if(mag > ON_THRESHOLD) {
      FMSimpleRecv(1);
    }
    else if (mag < OFF_THRESHOLD) {
      FMSimpleRecv(0);
    }
  }
}

byte block;
byte *bytes;
void FMSimpleRecv(byte smpl) {
  byte newBit;
  unsigned long stamp = millis();
  unsigned int diff = stamp - lastStamp;

  if(state != IDLE && diff > BIT_PERIOD*4) {
    state = IDLE;
    if(bytei>0) {
      msgs[bankIn].len = bytei;
      msgs[bankIn].ready = true;
      Serial.print("Block ready:");
      Serial.print(bankIn);
      Serial.print(" ");
      Serial.println(bytei);

      bankIn = (bankIn + 1)%NUM_MSGS;
      if(bankIn == bankProc || msgs[bankIn].ready==true) {
        Serial.println("TOO SLOW!");
      }
      msgs[bankIn].ready = false;
    }
    bytei = block = biti = 0;
  }  

  if(lastSmpl!=smpl){
    lastSmpl = smpl;
    //unsigned long stamp = millis();
    //Serial.print(smpl);
    //Serial.print(" ");
    //Serial.println(diff);
    //Serial.println(diff);
    switch(state) {
    case IDLE:
      if(smpl == 1) {
        lastStamp = stamp;
        state = HIGH_PULSE;
      }
      break;
    case LOW_PULSE:
      //newBit = 0;
      if(diff > BIT_PERIOD_2) {
        //newBit = 1;
        block |= 1<<biti;
      }
      lastStamp = stamp;
      state = HIGH_PULSE;

      if(++biti==8) {
        state = HIGH_PULSE;
        Serial.print(block,HEX);
        Serial.print(" ");
        Serial.println((char)block);
        msgs[bankIn].bytes[bytei++] = block;
        biti = block = 0;
        //if(bytei == bytej) Serial.println("TOO SLOW");
      }
      break;
    case HIGH_PULSE:
      state = LOW_PULSE;
      break;
    }
  }
}  


void FMSimpleSend(byte msg[], byte len) {

  disableADCInterrupt();
  #define INC_SIZE 1
  
  toneAC(OUT_TONES[1]);
  delay(BIT_PERIOD);

  for (int m = 0; m < len; m++) {
    for(int b = 0; b<8; b++) {
    
      for(int f=INC_SIZE; f<=BIT_PERIOD; f+=INC_SIZE) {
        toneAC(OUT_TONES[1] - ((OUT_TONES[1]-OUT_TONES[0])*f)/BIT_PERIOD);
        delay(INC_SIZE);
      }

      for(int f=INC_SIZE; f<=BIT_PERIOD; f+=INC_SIZE) {
        toneAC(OUT_TONES[0] + ((OUT_TONES[1]-OUT_TONES[0])*f)/BIT_PERIOD);
        delay(INC_SIZE);
      }
   
      if(((msg[m] >> b) & 0x1)) delay(BIT_PERIOD);
    }
  }

  for(int f=INC_SIZE; f<=BIT_PERIOD; f+=INC_SIZE) {
    toneAC(OUT_TONES[1] - ((OUT_TONES[1]-OUT_TONES[0])*f)/BIT_PERIOD);
    delay(INC_SIZE);
  }
  delay(BIT_PERIOD);
  noToneAC();

  enableADCInterrupt();

}












