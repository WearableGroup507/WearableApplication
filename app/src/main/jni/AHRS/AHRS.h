/************************************************************************
* Attitude and heading reference system                                
*
*	Reset
*	initialize all parameter of AHRS 
*
*
*	MahonyAHRSupdateIMU
*	use accelerator value calibrate gyroscope value and integral to compute hand orientation as quaternion unit
*	因為座標系的關係，轉換完後之quaternion數值需再轉90度才能在panel上畫出正確姿態
*
*	AHRSupdatePostion
*	input accelerator value and gyroscope value
*   first compute hand orientation
*   then use transform matrix to get G value and compute linear acc
*	finally integral the linear acc to get hand position
*	注意: 因為手套晶片上的感測器方向相反，因此在此作座標系轉換
* 
*
*	DownAccuracy
*	降低數值精度
*
*	QuaternProd
*   compute Quaternion product
*
*	Quatern2Matrix
*   transform quaternion to transform matrix 
*	
*	GetlinPostion
*	get hand tracking position 
*	@return(Vector3) hand position
*
*	GetOritation
*   get hand AHRS orientation(quaternion) result
*   @return(Quaternion) hand orientation
***********************************************************************/
#include <iostream>
#include <math.h>
#include <time.h>

#ifndef AHRS_H
#define AHRS_H


#include "Math/BasicMath.h"
#include "Math/Matrix3.h"
#include "Math/Quaternion.h"
#include "Math/Vector4.h"
#include "Kalman/SimpleKalman.h"
#include "Butterworth/ButterworthFilter.h"

class AHRS{

public:
	AHRS(float sampleFreq);
	
	void       Reset();
	void       MahonyAHRSupdateIMU(Vector3 gyro,Vector3 acc);
	void       AHRSupdatePostion(Vector3 gyro,Vector3 acc);
	double     DownAccuracy(double d);

	Vector4    QuaternProd (Vector4 quat, Vector4 refer);
	void       Quatern2Matrix();

	Vector3    GetlinPostion();
	Quaternion GetOritation();
	

private:

	
	float       m_samplePeriod;             //sample frequency
		
	float       m_kp;                       //加速度權重
	float       m_ki;                       //誤差積分增益

	//inertial navigation
	Quaternion  m_Quaternion;               //final hand orientation for output
	Vector3     m_gravityDirectionTrues;    //gravity value from sensor
	Vector3     m_gravityDirection;         //Estimated direction of gravity from AHRS system
	Vector3     m_integralFB;               //integral feedback
	Vector3     m_eulerAngle;               //euler angle which transform from quaternion             

	//position system
	Vector3     G;                          //加權計算得到的重力加速度值
	float       alpha;                      //加權值
	Matrix3     m_RotationMatrix;           //由quaternion轉換得到的旋轉矩陣
	Vector3     m_linAcc;                   //線性加速度值
	Vector3     m_linPosition;	            //積分得到的線性位置
	Vector3     m_linPositionHP;	        //經過Butter worth filter之high pass filter 後的位置
	Vector3     m_Position;                 //Final hand position for output

	//Butterworth Filter for velocity and position
	cButterworthFilter *m_pVelocityXHP, *m_pVelocityYHP, *m_pVelocityZHP; 
	cButterworthFilter *m_pPositionXHP, *m_pPositionYHP, *m_pPositionZHP;
};

#endif