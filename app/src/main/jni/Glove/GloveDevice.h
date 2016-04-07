/***********************************************************************
*	class GloveDevice
*   The state of Glove Device and AHRS transformation    
*
*   OpenCOMPort
*	@brief  Call function as open COM port
*	
*   						    
*   CloseCOMPort		    
*	@brief  Call function as close COM port
*
*
*	IsPortOpen
*	@brief  check if COM port is open
*	@return m_bIsPortOpen The values of boolean describe COM port is open or not
*
*	IsReading
*	check if COM port is reading
*	@return m_bKeepReading The values of boolean describe COM port is reading sensor value or not
*
*	InitializeKalmanFilter
*	initialize Kalman Filter
*
*	SetGloveData
*	use raw sensor data to set GloveData and transformation
*	@param GloveDatastru The data structure save raw sensor data.
*
*	ResetGloveData
*	reset GloveData
*
*	DataTransfer
*   use kalman filter to smooth feature and do AHRS transformation
*	@param id The value is the index of glove. Left hand : 0 , Right hand : 1
*	@return value The boolean param means the calibration process of GloveData is complete or not. 
*
/************************************************************************/
#include <iostream>
#include <time.h>
#include "GloveData.h"
#include "Kalman/SimpleKalman.h"
#include "AHRS/AHRS.h"
#define  PAYLOADLENGTH  24

class GloveDevice
{

public:
	GloveDevice(){
		CloseCOMPort();
		InitializeKalmanFilter();

		//«Å§iAHRS
		m_AHRS = new AHRS(0.02);
	}
	
	/************************************************************************/
	/*                  Glove Device Setting and initialize                 */
	/************************************************************************/
	void         OpenCOMPort(){
		m_bIsPortOpen = true;
		m_bKeepReading = true;
	}
	void         CloseCOMPort(){
		m_bIsPortOpen = false;
		m_bKeepReading = false;
	}
	bool         IsPortOpen(){
		return m_bIsPortOpen;
	}
	bool         IsReading(){
		return m_bKeepReading;
	}
	void         InitializeKalmanFilter()
	{
		m_kfFlex = new Kalman[FLEX_NUM];
		for(int i = 0 ; i < FLEX_NUM ; i++)
			m_kfFlex[i] = Kalman(0.0001, 0.05);

		m_kfGryo = new Kalman[GYRO_NUM];
		for(int i = 0 ; i < GYRO_NUM ; i++)
			m_kfGryo[i] = Kalman(0.0001, 0.005);

		m_kfAcc = new Kalman[ACC_NUM];
		for(int i = 0 ; i < ACC_NUM ; i++)
			m_kfAcc[i] = Kalman(0.0001, 0.005);
	}

	/************************************************************************/
	/*                          Glove Data Setting                          */
	/************************************************************************/
	CGloveData   m_gloveData;

	//Kalman Filter for Glove Sensor
	Kalman       *m_kfFlex;
	Kalman       *m_kfGryo;
	Kalman       *m_kfAcc ;

	AHRS         *m_AHRS;

	void         SetGloveData(GloveDatastru Data)
	{
		float flex[FLEX_NUM] = {Data._flex[0],Data._flex[1],Data._flex[2],Data._flex[3],Data._flex[4],
								Data._flex[5],Data._flex[6],Data._flex[7],Data._flex[8],Data._flex[9]};
		float gyro[GYRO_NUM] = {Data._gyro[0],Data._gyro[1],Data._gyro[2]};
		float acc[ACC_NUM]   = {Data._acc[0],Data._acc[1],Data._acc[2]};

		m_gloveData.SetRawData(flex,gyro,acc);
	}
	void         ResetGloveData()
	{
		m_gloveData.ResetData();
		m_AHRS->Reset();
	}

	// Transfer Glove Raw Data To Hand Feature 
	bool         DataTransfer(int id)
	{
		if(m_gloveData.ISCalibrate())
		{
			//smooth glove data
			for (int i = 0; i < FLEX_NUM; i++)
			{
				float rawData = this->m_gloveData.m_fFlexSensor[i];							 
				float kfFlexAngle = this->m_kfFlex[i].Update(rawData);				
				this->m_gloveData.m_fFingerAngle[i] = (90.0f - ((float)90.0f / ( this->m_gloveData.m_FingerProductValue[(int)id][i] - this->m_gloveData.m_FingerStartValue[(int)id][i])) * ((float)(this->m_gloveData.m_FingerProductValue[(int)id][i] - kfFlexAngle))) ;
			}

			for(int i = 0 ; i < GYRO_NUM ; i++)
			{
				float rawData = this->m_gloveData.m_AngleRate[i]*(3.1415/180);
				this->m_gloveData.m_fGyroRate[i] = this->m_kfGryo[i].Update(rawData); //radius
			}
			
			//Calculate hand orientation by AHRS algorithm
			Vector3 gyro(this->m_gloveData.m_fGyroRate[0],this->m_gloveData.m_fGyroRate[1],this->m_gloveData.m_fGyroRate[2]);
			Vector3 acce(this->m_gloveData.m_accRate[0]  ,this->m_gloveData.m_accRate[1]  ,this->m_gloveData.m_accRate[2]);
			this->m_AHRS->AHRSupdatePostion(gyro,acce);
			
			/*Update draw hand data*/
			this->m_gloveData.m_qRotation = this->m_AHRS->GetOritation();
			this->m_gloveData.m_fPosition = this->m_AHRS->GetlinPostion();
			 
			return true;
		}
		return false;
	}
private:
	//state parameter
	bool         m_bIsPortOpen;
	bool         m_bKeepReading;	
};