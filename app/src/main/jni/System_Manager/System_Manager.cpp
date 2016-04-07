/************************************************************************
*    System_Manager.cpp
*    @Author Chin-Feng Chang
*	 @Brief  The system manager manage the progress of this system
************************************************************************/

//------------------------------------------------------------------------------
// Includes
#include "System_Manager/System_Manager.h"

//------------------------------------------------------------------------------
// Methods

/**
*    @Function	InitialSystem
*    @Brief		initialize the variant in the class 
*/
void System_Manager::InitialSystem()
{
	/*System Parameter & component*/
	m_connectTime    = 0;
	m_playTimerCount = 0;

	dataQ[0].clear();
	dataQ[1].clear();
	motionDataQ[0].clear();
	motionDataQ[1].clear();
}

/**
*    @Function	SetSystemState
*    @Brief		set the system state
*    @Param     ss is the state of system, ref Enum SYSTEMSTATE
*/
void System_Manager::SetSystemState(SYSTEM_STATE ss){ m_sysState = ss;   }

/**
*    @Function	GetSystemState
*    @Brief		get the system state
*/
SYSTEM_STATE System_Manager::GetSystemState()       { return m_sysState; }

/**
*    @Function	SetWaveState
*    @Brief		set the state of wave graph
*    @Param		ws is the state of wave graph, ref Enum WAVE_STATE
*/
void System_Manager::SetWaveState(WAVE_STATE ws){ m_waveState = ws;   }

/**
*    @Function	GetWaveState
*    @Brief		get the state of wave graph
*/
WAVE_STATE System_Manager::GetWaveState()       { return m_waveState; }

/**
*    @Function	Display3DMotion
*    @Brief		call function of m_MotionViewPanel and draw 3D hand model
*/
//void System_Manager::Display3DMotion()
//{
//	m_MotionViewPanel.GLInitPanel();
//	m_MotionViewPanel.GLDrawScene();
//}

/**
*    @Function	LoadPlayData
*    @Brief		load one motion data by filename
*    @Param     filename is the name of motion data which u want to read.
*/
void System_Manager::LoadPlayData(std::string filename)
{
	std::vector<SignClip>                    tmpMotionSeq;
	tmpMotionSeq.swap(m_motionSeq);

	m_IOManager.OpenFileStream(1,filename);
	m_motionSeq = m_IOManager.ReadData();
	m_IOManager.CloseFileStream();
}

//------------------------------------------------------------------------------
// End of file
