/**
*    IO_Module.h
*    @Author Chin-Feng Chang
*	 @Brief  The class is a IO manager of motion data file to 
*            write/read motion data from database.
*/

//------------------------------------------------------------------------------
// Includes
#include <iostream>
#include <fstream> /* ifstream class */
#include <sstream> /* istringstream class */
#include <cstdlib> /* atoi   */
#include <string>

/*Math*/
#include "Math/BasicMath.h"
#include "Math/Vector3.h"

/*Sign Motion Database & Sign Motion Retrieval*/
#include "SignMotionData/SignMotionData.h"

#ifndef IO_MANAGER_H_
#define IO_MANAGER_H_

//------------------------------------------------------------------------------
// Class declaration
class IO_Manager{
public:
	
	void                                     OpenFileStream(int IO_Type,std::string fileName);							  //create or open motion file in the database
	void                                     RecordData(MotionDatastru currMotionDataL,MotionDatastru currMotionDataR);	  //write motion data into corresponding motion file
	std::vector<SignClip>                    ReadData();																  //read motion data from database
	void                                     CloseFileStream();															  //close all file stream param
	void                                     ClearMotionSeq();															  //clear the content of m_motionSeq

private:

	int                                      m_frameCount;         //record frame number counter
	std::vector<SignClip>                    m_motionSeq;          //temporal save motion data from database

	std::fstream                             m_fp;                 //file stream of motion data
	std::fstream                             m_posfp;              //file stream of position data
	std::fstream                             m_gyrofp;             //file stream of gyroscope data
};
#endif
//------------------------------------------------------------------------------
// End of file
