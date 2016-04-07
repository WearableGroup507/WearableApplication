/************************************************************************
*    SignMotionData.h
*    @Author Chin-Feng Chang
*	 @Brief  the class load motion database file from file
*            ,integrate motion data and generation the database file 
************************************************************************/

#ifndef SIGNMOTIONDATA_H_
#define SIGNMOTIONDATA_H_

//------------------------------------------------------------------------------
// Includes
#include <iostream>
#include <fstream> /* ifstream class */
#include <sstream> /* istringstream class */
#include <cstdlib> /* atoi   */
#include "Glove/GloveData.h"
#include "Math/BasicMath.h"
#include "Math/Quaternion.h"

#include <Android/log.h>

//------------------------------------------------------------------------------
// Class declaration
class SignClip{
public:
	int                 SignWordId;        //sign word index in the motion list
	CGloveData          m_readingData[2];
	std::vector<int>    SameValueInd;      //record the index of the same motion clip with current sign clip
	
	SignClip(float Lf[] = new float[10], float Rf[] = new float[10], 
	        Quaternion LQ = Quaternion(0,0,0,0), Quaternion RQ = Quaternion(0,0,0,0), 
	        Vector3 LA = Vector3::ZERO, Vector3 RA = Vector3::ZERO,
	        float LRSSI = 0.0f, float RRSSI = 0.0f) : SignWordId(0)
	{
		//left hand motion data		
		m_readingData[LEFTt].m_fFingerAngle[0] = Lf[0]; m_readingData[RIGHT].m_fFingerAngle[1] = Lf[1];
		m_readingData[LEFTt].m_fFingerAngle[2] = Lf[2]; m_readingData[RIGHT].m_fFingerAngle[3] = Lf[3];
		m_readingData[LEFTt].m_fFingerAngle[4] = Lf[4]; m_readingData[RIGHT].m_fFingerAngle[5] = Lf[5];
		m_readingData[LEFTt].m_fFingerAngle[6] = Lf[6]; m_readingData[RIGHT].m_fFingerAngle[7] = Lf[7];
		m_readingData[LEFTt].m_fFingerAngle[8] = Lf[8]; m_readingData[RIGHT].m_fFingerAngle[9] = Lf[9];
		m_readingData[LEFTt].m_qRotation = LQ;
		m_readingData[LEFTt].m_fPosition = LA;
		m_readingData[LEFTt].m_fRssi = LRSSI;
	    /// other features add below

		//right hand motion data
		m_readingData[RIGHT].m_fFingerAngle[0] = Rf[0]; m_readingData[RIGHT].m_fFingerAngle[1] = Rf[1];
		m_readingData[RIGHT].m_fFingerAngle[2] = Rf[2]; m_readingData[RIGHT].m_fFingerAngle[3] = Rf[3];
		m_readingData[RIGHT].m_fFingerAngle[4] = Rf[4]; m_readingData[RIGHT].m_fFingerAngle[5] = Rf[5];
		m_readingData[RIGHT].m_fFingerAngle[6] = Rf[6]; m_readingData[RIGHT].m_fFingerAngle[7] = Rf[7];
		m_readingData[RIGHT].m_fFingerAngle[8] = Rf[8]; m_readingData[RIGHT].m_fFingerAngle[9] = Rf[9];
		m_readingData[RIGHT].m_qRotation = RQ;
		m_readingData[RIGHT].m_fPosition = RA;
		m_readingData[RIGHT].m_fRssi = RRSSI;
	    /// other features add below
	}

	/*Compare SignClip Data*/
	//compare all feature value with other SignClip
	bool Compare(SignClip a)
	{
		//check the finger angle
		for(int i = 0 ; i < 10 ; i++)
		{
			if(a.m_readingData[0].m_fFingerAngle[i]!=m_readingData[0].m_fFingerAngle[i] || a.m_readingData[1].m_fFingerAngle[i]!=m_readingData[1].m_fFingerAngle[i] )
				return false;
		}

		if(a.m_readingData[0].m_qRotation!=m_readingData[0].m_qRotation || a.m_readingData[1].m_qRotation!=m_readingData[1].m_qRotation)
			return false;

		if(a.m_readingData[0].m_fPosition!=m_readingData[0].m_fPosition || a.m_readingData[1].m_fPosition!=m_readingData[1].m_fPosition)
			return false;

		if (a.m_readingData[0].m_fRssi != m_readingData[LEFTt].m_fRssi || a.m_readingData[0].m_fRssi != m_readingData[RIGHT].m_fRssi)
			return false;
	    /// other features add below

		return true;
	}                                                                 
	//compare the finger angle with other SignClip
	bool Compare(float* _LfFingerAngle,float* _RfFingerAngle)
	{
		//check the finger angle
		for(int i = 0 ; i < 10 ; i++)
		{
			if(_LfFingerAngle[i]!=m_readingData[0].m_fFingerAngle[i] || _RfFingerAngle[i]!=m_readingData[1].m_fFingerAngle[i])
				return false;
		}
		return true;
	} 
	//compare the orienatation with other SignClip
	bool Compare(Quaternion _LqRotation,Quaternion _RqRotation)
	{
		if(_LqRotation!=m_readingData[0].m_qRotation || _RqRotation!=m_readingData[1].m_qRotation)
			return false;
		return true;
	}
	//compare the acceleration with other SignClip
	bool Compare(Vector3 _LfPosition,Vector3 _RfPosition)
	{
		if(_LfPosition!=m_readingData[0].m_fPosition  || _RfPosition!=m_readingData[1].m_fPosition)
			return false;
		return true;
	}
	//compare the rssi with other SignClip
	bool Compare(float _LfRssi, float _RfRssi)
	{
		if (_LfRssi != m_readingData[LEFTt].m_fRssi || _RfRssi != m_readingData[RIGHT].m_fRssi)
			return false;
		return true;
	}
    /// other features add below
};

class SignMotionData{
public:	

	SignMotionData(){
 		std::cout << "Loading Database...\n";
		LoadMotionDatabase();
//		std::cout << "Origin Data Size:" << m_oriSignData.size() << std::endl;
//		std::cout << "Sign Data Size:" << m_signData.size() << std::endl;
//		std::cout << "Finger Data Size:" << m_signFingerData.size() << std::endl;
//		std::cout << "Quaternion Data Size:" << m_signQuaterData.size() << std::endl;
//		std::cout << "Accele Data Size:" << m_signAcceData.size() << std::endl;
//		std::cout << "Loading Database Finished...\n";
		__android_log_print(ANDROID_LOG_DEBUG, "SignMotionData::Constructor", "Origin Data Size: %d", m_oriSignData.size());
		__android_log_print(ANDROID_LOG_DEBUG, "SignMotionData::Constructor", "Sign Data Size: %d", m_signData.size());
		__android_log_print(ANDROID_LOG_DEBUG, "SignMotionData::Constructor", "Finger Data Size: %d", m_signFingerData.size());
		__android_log_print(ANDROID_LOG_DEBUG, "SignMotionData::Constructor", "Quaternion Data Size: %d", m_signQuaterData.size());
		__android_log_print(ANDROID_LOG_DEBUG, "SignMotionData::Constructor", "Accele Data Size: %d", m_signAcceData.size());
		__android_log_print(ANDROID_LOG_DEBUG, "SignMotionData::Constructor", "Rssi Data Size: %d", m_signRssiData.size());
        /// other features add below
		__android_log_print(ANDROID_LOG_DEBUG, "SignMotionData::Constructor", "Loading Database Finished...\n");
	}

	std::vector<SignClip>                    m_oriSignData;          //the ori recode frame
	std::vector<SignClip>                    m_signData;             //all different data frame need to encode
	std::vector<SignClip>                    m_signFingerData;       //all different finger data frame need to encode
	std::vector<SignClip>                    m_signQuaterData;       //all different Quaternion data frame need to encode
	std::vector<SignClip>                    m_signAcceData;         //all different Acce data frame need to encode
    std::vector<SignClip>                    m_signRssiData;         //all different RSSI data frame need to encode
    /// other features add below

	std::vector<int>                         m_motionSeqLength;      //motion sequence length of every motion sequence
	std::vector<std::string>                 m_signMotionFileName;   //sign language motion file name in the database
	
	int                                      m_maxLength;            //the maximum length of motion data 
	std::vector<int>                         m_motionLengthSum;      //the sum of length of motion data.

	/*Processing Motion Data*/
	void DataProcess();                                              //process database motion clip for encoding and save as a CSV file
	void IntegrateData();                                            //Integrate Data and Separate Feature

	void SetSingData(SignClip,int);                                  //Set m_signData
	void SetFingerData(SignClip,int);                                //Set m_signFingerData
	void SetQuaterData(SignClip,int);								 //Set m_signQuaterData
	void SetAcceData(SignClip,int);									 //Set m_signAcceData
    void SetRssiData(SignClip,int);                                  //Set m_signRssiData
    /// other features add below
	
	void SaveMotionDatabase(std::vector<SignClip>,std::string);		 //Write Separate Feature to corresponding File
	void SaveMotionFileNameFile();									 //Save Motion Data File Name
	void SaveMotionSeqLength();										 //Save Motion Data Sequence Length

	/*Load Motion Data from File*/									 
	void LoadMotionDatabase();										 //Load Motion Data Progress
	std::vector<SignClip> LoadFile(std::string);					 //Load Motion Database File
	void LoadMotionFileNameFile();									 //Load Motion Data File Name
	void LoadMotionSeqLength();										 //Load Motion Data Sequence Length	

	/*Clear Memery of Database*/
	void ClearDatabase();                                            //Clear up the data in the memory

private:
};

#endif
//------------------------------------------------------------------------------
// End of file
