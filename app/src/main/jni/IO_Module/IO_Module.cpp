/**
*    IO_Module.cpp
*    @Author Chin-Feng Chang
*	 @Brief  The class is a IO manager of motion data file to 
*            write/read motion data from database.
*/

//------------------------------------------------------------------------------
// Includes
#include "IO_Module/IO_Module.h"

//------------------------------------------------------------------------------
// Class Method

/**
*    @Function	OpenFileStream
*    @Brief		create or open motion file in the database
*	 @Param		IO_Type  : the type of read event(1) or write event(0)
*    @Param     fileName : file name of motion data
*/
void IO_Manager::OpenFileStream(int IO_Type,std::string fileName){

	std::string::size_type dotPos = fileName.find(".csv");
	std::string posFileName = fileName.substr(0,dotPos) + "_pos.csv";
	std::string gyroFileName = fileName.substr(0,dotPos) + "_gyro.csv";

	switch(IO_Type)  /*0:write, 1:read*/
	{
	case 0:
		//make frame counter become zero
		m_frameCount = 0;
		//open file stream
		m_fp.open(fileName.c_str(), std::ios::out);


		//write the first row data	
		m_fp << "Packet number,"
			<< "Left Pinky Angle 0,Left Pinky Angle 1,"
			<< "Left Ring Angle 0,Left Ring Angle 1,"
			<< "Left Middle Angle 0,Left Middle Angle 1,"
			<< "Left Index Angle 0,Left Index Angle 1,"
			<< "Left Thumb Angle 0,Left Thumb Angle 1,"
			<< "Left Quaternion w,Left Quaternion x,Left Quaternion y,Left Quaternion z,"
			<< "Left Accelerometer X (g),Left Accelerometer Y (g),Left Accelerometer Z (g),"
			<< "Right Pinky Angle 0,Right Pinky Angle 1,"
			<< "Right Ring Angle 0,Right Ring Angle 1,"
			<< "Right Middle Angle 0,Right Middle Angle 1,"
			<< "Right Index Angle 0,Right Index Angle 1,"
			<< "Right Thumb Angle 0,Right Thumb Angle 1,"
			<< "Right Quaternion w,Right Quaternion x,Right Quaternion y,Right Quaternion z,"
			<< "Right Accelerometer X (g),Right Accelerometer Y (g),Right Accelerometer Z (g)";

		m_posfp.open(posFileName.c_str(), std::ios::out);

		//write the first row data	
		m_posfp << "Packet number,"
			<< "Left position X (g),Left position Y (g),Left position Z (g),"
			<< "Right position X (g),Right position Y (g),Right position Z (g)";

		m_gyrofp.open(gyroFileName.c_str(), std::ios::out);

		//write the first row data	
		m_gyrofp << "Packet number,"
			<< "Left degree per second X (dps),Left degree per second Y (dps),Left degree per second Z (dps),"
			<< "Right degree per second X (dps),Right degree per second Y (dps),Right degree per second Z (dps)";

		break;
	case 1:
		m_frameCount = 0;
		std::cout << "Reading file name" << fileName.c_str() << "\n";
		//std::cout << "Reading position file name" << posFileName << "\n";
		m_fp.open(fileName.c_str());
		m_posfp.open(posFileName.c_str());
		break;
	}
}

/**
*    @Function	RecordData
*    @Brief		write motion data into corresponding motion file
*	 @Param		currMotionDataL : The motion data of left hand
*	 @Param		currMotionDataR : The motion data of right hand
*/
void IO_Manager::RecordData(MotionDatastru currMotionDataL,MotionDatastru currMotionDataR)
{
	
	//check the record frame number
	for(int i = 0 ; i < (m_frameCount+1)/10+1 ; i++)
		std::cout << "\b";
	std::cout << m_frameCount;
	

	//write motion data as csv file format
	//left hand first and right hand end
	m_fp<< "\n"
		<< m_frameCount << "," 
		<< currMotionDataL._fingerAngle[8] << "," << currMotionDataL._fingerAngle[9] << "," 
		<< currMotionDataL._fingerAngle[6] << "," << currMotionDataL._fingerAngle[7] << ","
		<< currMotionDataL._fingerAngle[4] << "," << currMotionDataL._fingerAngle[5] << ","
		<< currMotionDataL._fingerAngle[2] << "," << currMotionDataL._fingerAngle[3] << ","
		<< currMotionDataL._fingerAngle[0] << "," << currMotionDataL._fingerAngle[1] << ","
		<< currMotionDataL._rotation.w << ","  << currMotionDataL._rotation.x << "," << currMotionDataL._rotation.y << "," << currMotionDataL._rotation.z << ","
		<< currMotionDataL._acc.x << "," << currMotionDataL._acc.y << "," << currMotionDataL._acc.z << ","
		<< currMotionDataR._fingerAngle[8] << "," << currMotionDataR._fingerAngle[9] << "," 
		<< currMotionDataR._fingerAngle[6] << "," << currMotionDataR._fingerAngle[7] << ","
		<< currMotionDataR._fingerAngle[4] << "," << currMotionDataR._fingerAngle[5] << ","
		<< currMotionDataR._fingerAngle[2] << "," << currMotionDataR._fingerAngle[3] << ","
		<< currMotionDataR._fingerAngle[0] << "," << currMotionDataR._fingerAngle[1] << ","
		<< currMotionDataR._rotation.w << ","  << currMotionDataR._rotation.x << "," << currMotionDataR._rotation.y << "," << currMotionDataR._rotation.z << ","
		<< currMotionDataR._acc.x << "," << currMotionDataR._acc.y << "," << currMotionDataR._acc.z ;
	
	m_posfp<< "\n"
		   << m_frameCount << "," 
		   << currMotionDataL._pos.x << "," << currMotionDataL._pos.y << "," << currMotionDataL._pos.z << ","
		   << currMotionDataR._pos.x << "," << currMotionDataR._pos.y << "," << currMotionDataR._pos.z ;
	
	m_gyrofp<< "\n"
		<< m_frameCount << "," 
		<< currMotionDataL._gyro.x << "," << currMotionDataL._gyro.y << "," << currMotionDataL._gyro.z << ","
		<< currMotionDataR._gyro.x << "," << currMotionDataR._gyro.y << "," << currMotionDataR._gyro.z ;

	m_frameCount++;
}

/**
*    @Function	ReadData
*    @Brief		read motion data from database
*    @Return	motion sequence of the motion data as SignClip
*/
std::vector<SignClip> IO_Manager::ReadData(){
	
	ClearMotionSeq();

	std::string str_buf, str_posBuf;
	SignClip    curData;
	float       arrL[17],arrR[17],arrLP[3],arrRP[3];

	//first line of data
	getline(m_fp, str_buf); /* ��Ū�J�@���*/
	getline(m_posfp, str_posBuf);

	while(!m_fp.eof())
	{
		/*******************************************/
		/* Reading motion sequence of hand feature */
		/*******************************************/
		getline(m_fp, str_buf); /* ��Ū�J�@���*/
		std::istringstream stream(str_buf); /* �ϥ�istringstream */		

		/* �N stream ���e, �H�r�����j�v�@Ū�� */
		getline(stream, str_buf, ',');          //----> package number
		

		for(int i=0; i<34; ++i) { /* Ū��@���*/

			/* �N stream ���e, �H�r�����j�v�@Ū�� */
			getline(stream, str_buf, ',');

			if(i<17)
				/* ��atoi �N�r���ন���, �Y�n��B�I�ƥ�atof */
				arrL[i] = atof(str_buf.c_str());
			else 
				/* ��atoi �N�r���ন���, �Y�n��B�I�ƥ�atof */
				arrR[i-17] = atof(str_buf.c_str());
		}

		//left hand motion data		
		curData.m_readingData[0].m_fFingerAngle[8] = arrL[0],curData.m_readingData[0].m_fFingerAngle[9] = arrL[1];
		curData.m_readingData[0].m_fFingerAngle[6] = arrL[2],curData.m_readingData[0].m_fFingerAngle[7] = arrL[3];
		curData.m_readingData[0].m_fFingerAngle[4] = arrL[4],curData.m_readingData[0].m_fFingerAngle[5] = arrL[5];
		curData.m_readingData[0].m_fFingerAngle[2] = arrL[6],curData.m_readingData[0].m_fFingerAngle[3] = arrL[7];
		curData.m_readingData[0].m_fFingerAngle[0] = arrL[8],curData.m_readingData[0].m_fFingerAngle[1] = arrL[9];
		curData.m_readingData[0].m_qRotation = Quaternion(arrL[10],arrL[11],arrL[12],arrL[13]);
		curData.m_readingData[0].m_accRate[0]= arrL[14],curData.m_readingData[0].m_accRate[1]= arrL[15],curData.m_readingData[0].m_accRate[2]= arrL[16];
		//CalculatePosition(0,curData.m_readingData[0].m_qRotation,Vector3(arrL[14],arrL[15],arrL[16]));
		//curData.m_readingData[0].m_fPosition = linearPosition[0];

		//right hand motion data
		curData.m_readingData[1].m_fFingerAngle[8] = arrR[0],curData.m_readingData[1].m_fFingerAngle[9] = arrR[1];
		curData.m_readingData[1].m_fFingerAngle[6] = arrR[2],curData.m_readingData[1].m_fFingerAngle[7] = arrR[3];
		curData.m_readingData[1].m_fFingerAngle[4] = arrR[4],curData.m_readingData[1].m_fFingerAngle[5] = arrR[5];
		curData.m_readingData[1].m_fFingerAngle[2] = arrR[6],curData.m_readingData[1].m_fFingerAngle[3] = arrR[7];
		curData.m_readingData[1].m_fFingerAngle[0] = arrR[8],curData.m_readingData[1].m_fFingerAngle[1] = arrR[9];
		curData.m_readingData[1].m_qRotation = Quaternion(arrR[10],arrR[11],arrR[12],arrR[13]);
		curData.m_readingData[1].m_accRate[0]= arrR[14],curData.m_readingData[1].m_accRate[1]= arrR[15],curData.m_readingData[1].m_accRate[2]= arrR[16];
		//CalculatePosition(1,curData.m_readingData[1].m_qRotation,Vector3(arrR[14],arrR[15],arrR[16]));
		//curData.m_readingData[1].m_fPosition = linearPosition[1];
// 		std::cout << arrR[0] << "," << arrR[1] << "," 
// 			      << arrR[2] << "," << arrR[3] << "," 
// 				  << arrR[4] << "," << arrR[5] << "," 
// 				  << arrR[6] << "," << arrR[7] << ","
// 				  << arrR[8] << "," << arrR[9] << "\n";

		/********************************************/
		/* Reading motion sequence of hand position */
		/********************************************/
		getline(m_posfp, str_posBuf); /* ��Ū�J�@���*/
		std::istringstream posStream(str_posBuf); /* �ϥ�istringstream */
		getline(posStream,str_posBuf,',');
		for(int i=0; i<6; ++i) { /* Ū��@���*/
			/* �N stream ���e, �H�r�����j�v�@Ū�� */
			getline(posStream, str_posBuf, ',');

			if(i<3)
				/* ��atoi �N�r���ন���, �Y�n��B�I�ƥ�atof */
				arrLP[i] = atof(str_posBuf.c_str());
			else 
				/* ��atoi �N�r���ন���, �Y�n��B�I�ƥ�atof */
				arrRP[i-3] = atof(str_posBuf.c_str());
		}
		curData.m_readingData[0].m_fPosition = Vector3(arrLP[0],arrLP[1],arrLP[2]);
		curData.m_readingData[1].m_fPosition = Vector3(arrRP[0],arrRP[1],arrRP[2]);

		m_motionSeq.push_back(curData);
	}
	std::cout << "Read data end...\n";

	return m_motionSeq;
}

/**
*    @Function	CloseFileStream
*    @Brief		close all file stream param
*/
void IO_Manager::CloseFileStream( ){

	if(m_fp.is_open())    {m_fp.close();    m_fp.clear();  }

	if(m_posfp.is_open()) {m_posfp.close(); m_posfp.clear();}

	if(m_gyrofp.is_open()) {m_gyrofp.close(); m_gyrofp.clear();}
}

/**
*    @Function	ClearMotionSeq
*    @Brief		clear the content of m_motionSeq
*/
void IO_Manager::ClearMotionSeq()
{
	m_frameCount = 0;
	std::vector<SignClip>                    garbageCan;          //temp save motion data from database
	garbageCan.swap(m_motionSeq);
}
//------------------------------------------------------------------------------
// End of file
