/************************************************************************
*    System_Manager.h
*    @Author Chin-Feng Chang
*	 @Brief  The system manager manage the progress of this system
************************************************************************/

#ifndef SYSTEM_MANAGER_H__
#define SYSTEM_MANAGER_H__

//------------------------------------------------------------------------------
// Includes
#include "SignMotionData/SignMotionData.h"
#include "SLR_Manager/SLR_Manager.h"
#include "SignMotionData/SignMotionData.h"
#include "IO_Module/IO_Module.h"
//#include "GLRrawPanel/GLRrawPanel.h"

//------------------------------------------------------------------------------
// Definition
typedef std::deque<GloveDatastru> GloveDataQueue;
typedef std::deque<MotionDatastru> MotionDataQueue;

//------------------------------------------------------------------------------
// Enum
typedef enum {
	IDLE,
	CONNECT,
	READ,
	PLAY,
	PAUSE,
	RECORD,
	RETRIEVAL,
	OUTPUT
}SYSTEM_STATE;

typedef enum {
	EMPTY,
	GYROL,
	GYROR,
	ACCL,
	ACCR
}WAVE_STATE;

//------------------------------------------------------------------------------
// Class declaration
class System_Manager{
public:

	System_Manager():m_sysState(IDLE),m_waveState(EMPTY){  InitialSystem();  }


	/*System Parameter & component*/
	int                                      m_connectTime;                                            //the time of connect to glove
	int                                      m_playTimerCount;                                         //the time of play motion
	bool                                     m_IsFileOpened;                                           //check is the file opened
	bool                                     m_Ismodified;		                                       //check m_motionSeq is cut or not
																                                       
	/*Motion Data*/												                                       
	GloveDataQueue                           dataQ[2];                                                 // raw data from glove
	MotionDataQueue                          motionDataQ[2];                                           // motion data for record and retrieval
	std::vector<SignClip>                    m_motionSeq;                                              //temp save motion data from database
																                                       
	/*Working Module*/											                                       
	SLR_Manager                              m_SLRM;                                                   //sign language recognition manager
	IO_Manager                               m_IOManager;                                              //IO Manager
//	GLRrawPanel                              m_MotionViewPanel;                                        //draw panel

	/*Member Function*/																				   
	void                                     InitialSystem();										   //initialize the variant in the class 
	
	/*System State function*/																		   
	void                                     SetSystemState(SYSTEM_STATE);							   //set the system state
	SYSTEM_STATE                             GetSystemState();                                         //get system state
																									   

	void                                     SetWaveState(WAVE_STATE);								   //set the state of wave graph
	WAVE_STATE                               GetWaveState();                                           //get system state
																									   
	/*Draw Function*/																				   
//	void                                     Display3DMotion();										   //call function of m_MotionViewPanel and draw 3D hand model
	void                                     LoadPlayData(std::string filename);					   //load one motion data by filename
private:																							   
	SYSTEM_STATE                             m_sysState;                                               // the state of system
	WAVE_STATE                               m_waveState;                                              // the state of wave graph
};

#endif
//------------------------------------------------------------------------------
// End of file
