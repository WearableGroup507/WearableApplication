/************************************************************************
* Define Glove Sensor Value and Hand Value                              
* (1) flex value                                                       
* (2) gyro value                                                       
* (3) acc value                                                        
*
*	Init
*	initialize all parameter of class CGloveData
*
*	Zero()
*	�Ҧ��ƭ��k�s
*
*	ISCalibrate
*	return the Glove is calibrated or not.
*
*	SetRawData
*	use reading raw data from glove to set the parameter of sensor in class CGlove
*
*	Raw2Rate
*	sample the first 50 value as zero-level value to finish calibration process then transform raw sensor data to rate data
*
*	ResetData()
*	reset sensor data and rate data to be zero
*
*	ResetCalibration
*	reset calibration process
************************************************************************/

#include <iostream>
#ifndef CGLOVEDATA_H
#define CGLOVEDATA_H 

#include "Defs/Defs.h"
#include "Math/BasicMath.h"
#include "Math/Quaternion.h"
#include "Math/Vector3.h"

//------------------------------------------------------------------------------
// Data Structure

/*Save Raw Sensor Value of Glove*/
struct GloveDatastru{
public:
	float _flex[NUM_FLEX];
	float _gyro[NUM_GYRO];
	float _acc[NUM_ACCE];
};

/*Using finger angle & quaternion & acc value as motion feature*/
struct MotionDatastru{
public:
	float      _fingerAngle[NUM_FLEX];
	Quaternion _rotation;
	Vector3    _acc;
	Vector3    _pos;
	Vector3    _gyro;
    float      _rssi;
    /// other features add below
};

class CGloveData
{
public:
	/************************************************************************/
	/*                         Public method                                */
	/************************************************************************/
	/*variable*/
	int          m_sampleNum;
	int          m_connectTime;
	bool         m_IsCalibrate;

	CGloveData():m_connectTime(0),m_gyroSentivity(8.75 /1000){ Init(); }
	void Init();
	void Zero();                                                                         //clear all values to zero
	bool ISCalibrate() {return m_IsCalibrate;}
	void SetRawData(float flex[NUM_FLEX],float gyro[NUM_GYRO],float acc[NUM_ACCE]);	
	void Raw2Rate();                                                                     //convert raw data to the rate of angle and accelerate	
	void ResetData();                                                                    //Reset Glove Data
	void ResetCalibration();                                                             //Reset Calibration flag and variable

	/************************************************************************/
	/*                         Flex Sensor                                */
	/************************************************************************/
	float        m_fFlexSensor[NUM_FLEX];                                                //flex sensor raw data
	float        m_FingerStartValue[2][NUM_FLEX];
	float        m_FingerProductValue[2][NUM_FLEX];
	float        m_fFingerAngle[NUM_FLEX];                                               //final finger angle	
	void         ZeroFlex();                                                             //clear flex values to zero	
	void         ZeroFingerAngle();                                                      //clear flex values to zero


	/************************************************************************/
	/*                         Gyroscope                                    */
	/************************************************************************/
	float        m_fGyroSensor[NUM_GYRO];                                                //Gyroscope raw data
	double       m_gyroSentivity;												         
	int          m_gyroDcOffset[NUM_GYRO];										         //Gyroscope zero-level value
	float        m_AngleRate[NUM_GYRO];	                                                 //final rotation data	
	float        m_fGyroRate[NUM_GYRO];                                                   			                                                 
	void         ZeroGyro();                                                             //clear gyro values to zero		
	void         ZeroRotationAngle();                                                    //clear gyro values to zero
	

	/************************************************************************/
	/*                         Accelerator                                  */
	/************************************************************************/
	float        m_fAccSensor [NUM_ACCE];                                                 //Accelerator raw data
	int          m_accDcOffset[NUM_ACCE];                                                 
	float        m_accRate[NUM_ACCE];                                                     //Acceleration      
	Vector3      m_fPosition;                                                            
	Quaternion   m_qRotation;	                                                         //Feature
	void         ZeroAcc();                                                              //clear acc values to zero	
	void         ZeroPosition();                                                         //clear acc values to zero
	
	
	/************************************************************************/
	/*                         RSSI                                         */
	/************************************************************************/
	float m_fRssi;
	void ZeroRssi();
    /// other features add below
	

	/************************************************************************/
	/*                         Data Operation                               */
	/************************************************************************/
	CGloveData& operator=(const CGloveData& gloveData)
	{
		m_qRotation = gloveData.m_qRotation;	
		m_fPosition = gloveData.m_fPosition;

		for(int i = 0 ; i < NUM_ACCE ; i++)
			m_accRate[i]   = gloveData.m_accRate[i];

		for (int i = 0; i < NUM_FLEX; i++)
			m_fFingerAngle[i] = gloveData.m_fFingerAngle[i];

		return *this;
	}

	CGloveData& operator+=(const CGloveData& gloveData)
	{
		this->m_qRotation = this->m_qRotation + gloveData.m_qRotation;	
		this->m_fPosition += gloveData.m_fPosition;


		for(int i = 0 ; i < NUM_ACCE ; i++)
			this->m_accRate[i]  += gloveData.m_accRate[i];

		for (int i = 0; i < NUM_FLEX; i++)
			this->m_fFingerAngle[i] += gloveData.m_fFingerAngle[i];

		return *this;
	}

	CGloveData operator/(const float& divNum)
	{
		CGloveData subGloveData;

		subGloveData.m_qRotation = this->m_qRotation * (1/divNum);	
		subGloveData.m_fPosition = this->m_fPosition / divNum;

		for(int i = 0 ; i < NUM_ACCE ; i++)
			subGloveData.m_accRate[i]  = this->m_accRate[i] / divNum;

		for (int i = 0; i < NUM_FLEX; i++)
			subGloveData.m_fFingerAngle[i] = this->m_fFingerAngle[i]/divNum;

		return subGloveData;
	}

	CGloveData operator-(const CGloveData& gloveData)
	{

		CGloveData subGloveData;

		subGloveData.m_qRotation = Quaternion(this->m_qRotation.w - gloveData.m_qRotation.w,
			this->m_qRotation.x - gloveData.m_qRotation.x,
			this->m_qRotation.y - gloveData.m_qRotation.y,
			this->m_qRotation.z - gloveData.m_qRotation.z);

		//std::cout << subGloveData.m_qRotation << " = \n" << this->m_qRotation << "-\n" << gloveData.m_qRotation << "\n";

		subGloveData.m_fPosition = Vector3(this->m_fPosition.x - gloveData.m_fPosition.x,
			this->m_fPosition.y - gloveData.m_fPosition.y,
			this->m_fPosition.z - gloveData.m_fPosition.z); 

		//std::cout << subGloveData.m_fPosition << " = \n" << this->m_fPosition << "-\n" << gloveData.m_fPosition << "\n";

		for(int i = 0 ; i < NUM_ACCE ; i++)
		{
			subGloveData.m_accRate[i]  = this->m_accRate[i] - gloveData.m_accRate[i];
			//std::cout << subGloveData.m_accRate[i] << " = \n" << this->m_accRate[i] << "-\n" << gloveData.m_accRate[i] << "\n";
		}

		for (int i = 0; i < NUM_FLEX; i++)
			subGloveData.m_fFingerAngle[i] = this->m_fFingerAngle[i] - gloveData.m_fFingerAngle[i];

		return subGloveData;
	}

	CGloveData operator^(const CGloveData& gloveData)
	{
		CGloveData subGloveData;

		subGloveData.m_qRotation = Quaternion(this->m_qRotation.w * gloveData.m_qRotation.w,
			this->m_qRotation.x * gloveData.m_qRotation.x,
			this->m_qRotation.y * gloveData.m_qRotation.y,
			this->m_qRotation.z * gloveData.m_qRotation.z);

		subGloveData.m_fPosition = Vector3(this->m_fPosition.x * gloveData.m_fPosition.x,
			this->m_fPosition.y * gloveData.m_fPosition.y,
			this->m_fPosition.z * gloveData.m_fPosition.z); 

		for(int i = 0 ; i < NUM_ACCE ; i++)
			subGloveData.m_accRate[i]  = this->m_accRate[i] * gloveData.m_accRate[i];

		for (int i = 0; i < NUM_FLEX; i++)
			subGloveData.m_fFingerAngle[i] = this->m_fFingerAngle[i] * gloveData.m_fFingerAngle[i];

		return subGloveData;
	}


	
	/************************************************************************/
	/*                      Data Structure Transfer                         */
	/************************************************************************/
	GloveDatastru CGloveData2GloveDatastru()
	{
		GloveDatastru targetData;
		return targetData;
	}

	MotionDatastru CGloveData2MotionDatastru()
	{
		MotionDatastru targetData;
		for (int i = 0; i < NUM_FLEX; i++)
			targetData._fingerAngle[i] = m_fFingerAngle[i];
		targetData._rotation = m_qRotation;
		targetData._acc      = Vector3 (m_accRate[0] ,m_accRate[1] ,m_accRate[2]);		
		targetData._pos      = m_fPosition;
		targetData._gyro     = Vector3(m_fGyroRate[0],m_fGyroRate[1],m_fGyroRate[2]);
		targetData._rssi	 = m_fRssi;
	    /// other features add below
		return targetData;
	}
};

#endif
