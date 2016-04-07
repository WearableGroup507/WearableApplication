#ifndef DEFS_H
#define DEFS_H

#define NUM_GYRO    3       // number of gyroscope axis
#define NUM_ACCE    3       // number of accelerometer axis
#define NUM_FLEX    10      // number of flex sensors
#define NUM_TOUCH   10      // number of touch sensors
#define NUM_FEATURE 4       // number of features

#define DOF_FINGER  10	    // flex sensor DOF of single hand
#define DOF2_FINGER 20	    // flex sensor DOF of both hand

#define DOF_TOUCH   10      // touch sensor DOF of single hand
#define DOF2_TOUCH  20      // touch sensor DOF of both hand

#define DOF_QUAT    4	    // quaternion DOF of single hand
#define DOF2_QUAT   8	    // quaternion DOF of both hand

#define DOF_ACCE    3	    // acceleration DOF of single hand
#define DOF2_ACCE   6	    // acceleration DOF of both hand

#define DOF_EULER   3	    // rotation euler angle DOF of single hand
#define DOF2_EULER  6	    // rotation euler angle DOF of both hand

#define DOF_RSSI    1	    // BLE RSSI DOF of single hand
#define DOF2_RSSI   2	    // BLE RSSI DOF of both hand

#define DOF_SIGN    18      // sign DOF of single hand  (flex + quat + acce + rssi)
#define DOF2_SIGN   36      // sign DOF of both hand

#define SAMPLENUM   50      // calibrate samples number

#define LEFTt       0       // left hand
#define RIGHT       1       // right hand

#define KNN        20
//#define KNN        50
#define RTIMELIMIT 150

#define PickFrameSize 10
#define OP2TimelineInterval 30
#define OP2TIMEINTERVAL 20

#endif
