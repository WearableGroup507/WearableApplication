/************************************************************************/
/* Use Three KD-Tree and Encoding for three kind of motion data         */
/* All real time motion data input this class and separate to           */
/* three OLNG to retrieval motion sequence                              */
/************************************************************************/

#ifndef SLR_MANAGER_H
#define SLR_MANAGER_H

//------------------------------------------------------------------------------
// Include
#include <iostream>
#include <fstream> // ifstream class 
#include <sstream> // istringstream class 
#include <cstdlib> // atoi   
#include <queue>   // data queue
#include <algorithm>

// Definitions
#include "Defs/Defs.h"

//Math
#include "Math/BasicMath.h"
#include "Math/Vector3.h"

//Glove Data
#include "Glove/GloveData.h"

//Sign Motion Database & Sign Motion Retrieval
#include "SignMotionData/SignMotionData.h"

//KD-Tree
#include "KD_Tree/kdtree2.hpp"
#include <boost/multi_array.hpp>

////OLNG
//#include "VoiceOutput/VoiceOutput.h"
#include "OLNG/OLNG.h"

//Weight Generator
#include "WeightGenerator/WeightGenerator.h"

//------------------------------------------------------------------------------
// Definition
typedef boost::multi_array<float,2> array2f;

typedef struct wordEnergy{
	int wordId;
	int frameId;
	float energyValue;
	float allDistance;
	int showNum;
}WE;

//------------------------------------------------------------------------------
// Enum
typedef enum {
	GLOVE,
	WATCH,
	ACC
}FeatureType;


//------------------------------------------------------------------------------
// STL function definition 
bool struct_cmp_by_engValue(const WE &a,const WE &b);                                                                               //STL compare by energy value 
bool struct_cmp_by_frameId(const WE &a,const WE &b);                                                                                //STL compare by frame index


//------------------------------------------------------------------------------
// Class Definition 
class SLR_Manager
{
public:
	SLR_Manager(){ Init(); }

	/************************************************************************/
	/*                           Manager Member                             */
	/************************************************************************/
	//Data
	int                                      m_retrivelTime;																	   //retrieval time counter
	bool                                     m_IsRecognition;                                                                      //check if recognition success or not
	bool                                     m_startRecognition;																   //check if the recognition progress is start or not
	std::string                              m_candidateWord;																	   //the result word
	FeatureType                              m_featureType;																		   //the input type of feature
	std::fstream                             m_fp;																				   //the file stream to write system log
																																   
	//System Module																												   
	SignMotionData                           m_database;                                                                           //Sign Language Motion Database
//	VoiceOutput                              m_voice;																			   //the audio manager to play audio as recognition success

	/************************************************************************/
	/*                      Manager Member Function                         */
	/************************************************************************/
	void                                     Init();                                                                               //initial the sign language recognition system
	void                                     SignEncodeing();                                                                      //sign motion clip encoding
	void                                     RecognizeProcessing(MotionDatastru currMotionDataL,MotionDatastru currMotionDataR);   //recognition process
	void                                     Reset();																			   //reset system 
	void                                     SetFeatureType(std::string featureTypeId);											   //set input feature type

	/************************************************************************/
	/*                          preprocess Encoding                         */
	/************************************************************************/
	//Finger Angle 																												   
	array2f                                  fingerRealdata;																	   //all finger angle data in the database
	kdtree2*                                 fingerTree;																		   //KD-Tree of finger angle
	kdtree2_result_vector                    fingerResult;																		   //the KNN search result of finger angle
	
	//Quaternion KD-Tree																										   
	array2f                                  quaterRealdata;																	   //all orientation data in the database
	kdtree2*                                 quaterTree;																		   //KD-Tree of orientation
	kdtree2_result_vector                    quaterResult;																		   //the KNN search result of orientation
	
	//Accelerator value KD-Tree																									   
	array2f                                  accelRealdata;																		   //all acceleration data in the database
	kdtree2*                                 accelTree;																			   //KD-Tree of acceleration
	kdtree2_result_vector                    accelResult;																		   //the KNN search result of acceleration
	
	//RSSI value KD-Tree
	array2f                                  rssiRealdata;
	kdtree2*                                 rssiTree;
	kdtree2_result_vector                    rssiResult;
    /// other features add below
	
	//Encoding motion data																										   
	void                                     FingerEncoding();																	   //encoding finger angle data
	void                                     QuaterEncoding();																	   //encoding orientation data 
	void                                     AccelEncoding();																	   //encoding acceleration data
    void                                     RssiEncoding();
    /// other features add below

	/************************************************************************/
	/*                         Vocabulary Weight                            */
	/************************************************************************/
	std::vector<WeightGenerator>             m_FeatureWeight;		                                                               //the vocabulary feature weight
	void                                     InitialWeight();		                                                               //initialize the feature weight
	void                                     WeightNormalization();	                                                               //normalize the feature weight


	/************************************************************************/
	/*                          Retrieval process                           */
	/************************************************************************/
	OLNG                                     m_finger_OLNG;							                                               //the OLNG of finger angle
	OLNG                                     m_quater_OLNG;							                                               //the OLNG of orientation
	OLNG                                     m_accel_OLNG;							                                               //the OLNG of acceleration
	OLNG                                     m_rssi_OLNG;
    /// other features add below
																					                                               
	void                                     MotionRetrieval(float *,float *);		                                               //retrieval the similar finger angle 
	void                                     MotionRetrieval(Quaternion,Quaternion);                                               //retrieval the similar orientation 
	void                                     MotionRetrieval(Vector3,Vector3); 		                                               //retrieval the similar acceleration 
	void                                     MotionRetrieval(float, float);
    /// other features add below
		
	
	/************************************************************************/
	/*                           Optimization                               */
	/************************************************************************/
	bool                                     IsRecognition;					                                                        //check it is recognition success or not
	int                                      m_optimizationTime;			                                                        //the time of optimization process
	int                                      m_timeStep;					                                                        //the time step 
																			                                                        
	std::vector<WE>                          m_candidateWord_Final;			                                                        //the set of candidate word in the optimization stage
	std::vector<int>                         m_candidateWord_local;			                                                        //the set of candidate word in the retrieval stage 
	//std::vector<std::vector<WE>>             m_GlobalMotionGraph;			                                                        //
	std::vector<std::vector<WE>>             m_GlobalMotionGraph_Final;		                                                        //
	std::string                              m_lastTimeOutputWordId;		                                                        //
	std::vector<int>                         m_RetrievalWordBox;			                                                        //
	std::vector<WE>                          m_TimeIntervalBox;				                                                        //
	
	
	void                                     GlobalOptimization();			                                                        //Optimization process
	void                                     ResetGlobalOptimization();		                                                        //reset the optimization process

	std::vector<WE>                          LocalOptimization();			                                                        //the optimization process at time t and the feature type is GLOVE
	std::vector<WE>                          IMUsLocalOptimization();		                                                        //the optimization process at time t and the feature type is WATCH
	std::vector<WE>                          ACCLocalOptimization();		                                                        //the optimization process at time t and the feature type is ACCELERAMETER
	std::vector<WE>                          RssiLocalOptimization();		                                                        //the optimization process at time t and the feature type is RSSI
	void                                     InitTimelineBox();				                                                        //initial the time line box 
	void                                     ClearCandidateWord();			                                                        //clear the memory of candidate word
	void                                     UpdateTimelineBox();			                                                        //update the time line box
	float                                    SqrtDistance(float* lfa,float* rfa,float* lfat,float* rfat);				            //compute the cosine similarity between finger angle 
	float                                    SqrtDistance(Quaternion lot,Quaternion rot,Quaternion lott,Quaternion rott);           //compute the cosine similarity between orientatioin 
	float                                    SqrtDistance(Vector3 lacc,Vector3 racc,Vector3 lacct,Vector3 racct);		            //compute the cosine similarity between acceleration 
	float                                    SqrtDistance(float lrssi, float rrssi, float lrssit, float rrssit);
    /// other features add below
};
#endif
//------------------------------------------------------------------------------
// End of file
