/************************************************************************
*    SignMotionData.cpp
*    @Author Chin-Feng Chang
*	 @Brief  the class load motion database file from file
*            ,integrate motion data and generation the database file 
************************************************************************/

//------------------------------------------------------------------------------
// Includes
#include "SignMotionData/SignMotionData.h"
#include "EasyLog.h"

//------------------------------------------------------------------------------
// Methods

/**
*    @Function	DataProcess
*    @Brief	    Processing Motion Data
*				Step 1. Clear Database
*				Step 2. Integrate Data and Separate Feature
*				Step 3. Write Separate Feature to corresponding File
*/
void SignMotionData::DataProcess()
{
	system("cls");

	std::cout << "Data Processing...\n";

	ClearDatabase();
	IntegrateData();
	SaveMotionDatabase(m_signData,"..\\Data\\CGALDatabase\\Database\\SignData.csv");
	SaveMotionDatabase(m_signFingerData,"..\\Data\\CGALDatabase\\Database\\SignFingerData.csv");
	SaveMotionDatabase(m_signQuaterData,"..\\Data\\CGALDatabase\\Database\\SignQuaterData.csv");
	SaveMotionDatabase(m_signAcceData,"..\\Data\\CGALDatabase\\Database\\SignAcceData.csv");
    SaveMotionDatabase(m_signRssiData,"..\\Data\\CGALDatabase\\Database\\SignRssiData.csv");
    /// other features add below
	SaveMotionFileNameFile();
	SaveMotionSeqLength();

	std::cout << "Data Processing Finished...\n";
}

/**
*    @Function	IntegrateData
*    @Brief		Integrate Data from motion data file and separate to each file correspond to the feature
*/
void SignMotionData::IntegrateData()
{
	//Remove original Data
	std::remove("..\\Data\\CGALDatabase\\Database\\OriSignData.csv");
	/************************************************************************/
	/*���Xdatabase���C���ɮ׶i���X                                        */
	/*�åB�ھڤ��PFeature�i����A�s��                                     */
	/************************************************************************/
	std::fstream buildfp;
	std::fstream OSfp;
	OSfp.open("..\\Data\\CGALDatabase\\Database\\OriSignData.csv", std::ios::out);
	for(int i = 0 ; i < m_signMotionFileName.size() ; i++)
	{		
		std::string filename = "..\\Data\\CGALDatabase\\" +m_signMotionFileName[i];

		buildfp.open(filename.c_str(), std::fstream::in);

		//Read CGAL Glove Data
		std::string str_buf;
		SignClip    curData;
		float       arrL[17],arrR[17];
		float       packageNum;

		//first line of data
		getline(buildfp, str_buf); /* ��Ū�J�@���*/

		std::cout << "processing file:" << filename << "\n";
		while(!buildfp.eof())
		{
			SignClip tmpSignClip;         //save sign motion data temporarily

			/*Read the sign motion clip*/
			getline(buildfp, str_buf);             // ��Ū�J�@���
			std::istringstream stream(str_buf); // �ϥ�istringstream

			/* �N stream ���e, �H�r�����j�v�@Ū�� */
			getline(stream, str_buf, ',');          //----> package number
			packageNum = atoi(str_buf.c_str());


			for(int j=0; j<34; ++j) {               // Ū��@���
				/* �N stream ���e, �H�r�����j�v�@Ū�� */
				getline(stream, str_buf, ',');
				if(j<17)					
					arrL[j] = atof(str_buf.c_str());   // ��atoi �N�r���ন���, �Y�n��B�I�ƥ�atof
				else 					
					arrR[j-17] = atof(str_buf.c_str());// ��atoi �N�r���ন���, �Y�n��B�I�ƥ�atof
			}

			//left hand motion data		
			tmpSignClip.m_readingData[0].m_fFingerAngle[8] = arrL[0],tmpSignClip.m_readingData[0].m_fFingerAngle[9] = arrL[1];
			tmpSignClip.m_readingData[0].m_fFingerAngle[6] = arrL[2],tmpSignClip.m_readingData[0].m_fFingerAngle[7] = arrL[3];
			tmpSignClip.m_readingData[0].m_fFingerAngle[4] = arrL[4],tmpSignClip.m_readingData[0].m_fFingerAngle[5] = arrL[5];
			tmpSignClip.m_readingData[0].m_fFingerAngle[2] = arrL[6],tmpSignClip.m_readingData[0].m_fFingerAngle[3] = arrL[7];
			tmpSignClip.m_readingData[0].m_fFingerAngle[0] = arrL[8],tmpSignClip.m_readingData[0].m_fFingerAngle[1] = arrL[9];
			tmpSignClip.m_readingData[0].m_qRotation = Quaternion(arrL[10],arrL[11],arrL[12],arrL[13]);
			tmpSignClip.m_readingData[0].m_fPosition = Vector3(arrL[14],arrL[15],arrL[16]);
            tmpSignClip.m_readingData[LEFTt].m_fRssi = arrL[17];
            /// other features add below

			//right hand motion data
			tmpSignClip.m_readingData[1].m_fFingerAngle[8] = arrR[0],tmpSignClip.m_readingData[1].m_fFingerAngle[9] = arrR[1];
			tmpSignClip.m_readingData[1].m_fFingerAngle[6] = arrR[2],tmpSignClip.m_readingData[1].m_fFingerAngle[7] = arrR[3];
			tmpSignClip.m_readingData[1].m_fFingerAngle[4] = arrR[4],tmpSignClip.m_readingData[1].m_fFingerAngle[5] = arrR[5];
			tmpSignClip.m_readingData[1].m_fFingerAngle[2] = arrR[6],tmpSignClip.m_readingData[1].m_fFingerAngle[3] = arrR[7];
			tmpSignClip.m_readingData[1].m_fFingerAngle[0] = arrR[8],tmpSignClip.m_readingData[1].m_fFingerAngle[1] = arrR[9];
			tmpSignClip.m_readingData[1].m_qRotation = Quaternion(arrR[10],arrR[11],arrR[12],arrR[13]);
			tmpSignClip.m_readingData[1].m_fPosition = Vector3(arrR[14],arrR[15],arrR[16]);
            tmpSignClip.m_readingData[RIGHT].m_fRssi = arrR[17];
            /// other features add below

			//save the ori motion data
			tmpSignClip.SignWordId = i;
			//add the self Id
			tmpSignClip.SameValueInd.push_back(m_oriSignData.size());	

			//Push this Sign Clip into origin Sign Database vector
			m_oriSignData.push_back(tmpSignClip);

			//Write SignClip into file
			OSfp<< tmpSignClip.SignWordId<< ","
				<< tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[8] << "," << tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[9] << "," 
				<< tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[6] << "," << tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[7] << ","
				<< tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[4] << "," << tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[5] << ","
				<< tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[2] << "," << tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[3] << ","
				<< tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[0] << "," << tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[1] << ","
				<< tmpSignClip.m_readingData[LEFTt].m_qRotation.w     << "," << tmpSignClip.m_readingData[LEFTt].m_qRotation.x     << "," 
				<< tmpSignClip.m_readingData[LEFTt].m_qRotation.y     << "," << tmpSignClip.m_readingData[LEFTt].m_qRotation.z     << ","
				<< tmpSignClip.m_readingData[LEFTt].m_fPosition.x     << "," << tmpSignClip.m_readingData[LEFTt].m_fPosition.y     << "," << tmpSignClip.m_readingData[0].m_fPosition.z << ","
				<< tmpSignClip.m_readingData[LEFTt].m_fRssi           << ","
				/// other features add below
				<< tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[8] << "," << tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[9] << "," 
				<< tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[6] << "," << tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[7] << ","
				<< tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[4] << "," << tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[5] << ","
				<< tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[2] << "," << tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[3] << ","
				<< tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[0] << "," << tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[1] << ","
				<< tmpSignClip.m_readingData[RIGHT].m_qRotation.w     << "," << tmpSignClip.m_readingData[RIGHT].m_qRotation.x     << ","
				<< tmpSignClip.m_readingData[RIGHT].m_qRotation.y     << "," << tmpSignClip.m_readingData[RIGHT].m_qRotation.z     << ","
				<< tmpSignClip.m_readingData[RIGHT].m_fPosition.x     << "," << tmpSignClip.m_readingData[RIGHT].m_fPosition.y     << "," << tmpSignClip.m_readingData[1].m_fPosition.z << ","
				<< tmpSignClip.m_readingData[RIGHT].m_fRssi << "\n";
				/// other features add below
			
			SetSingData(tmpSignClip,packageNum);

			SignClip signFingerData(tmpSignClip.m_readingData[0].m_fFingerAngle,tmpSignClip.m_readingData[1].m_fFingerAngle);
			signFingerData.SameValueInd.push_back(m_oriSignData.size()-1);
			SetFingerData(signFingerData,packageNum);	

			float ZeroAngle[10] = {0,0,0,0,0,0,0,0,0,0};					
			SignClip signQuaterData(ZeroAngle,ZeroAngle,tmpSignClip.m_readingData[0].m_qRotation,tmpSignClip.m_readingData[1].m_qRotation,Vector3::ZERO,Vector3::ZERO);	
			signQuaterData.SameValueInd.push_back(m_oriSignData.size()-1);
			SetQuaterData(signQuaterData,packageNum);

			SignClip signAcceData(ZeroAngle,ZeroAngle,Quaternion(0,0,0,0),Quaternion(0,0,0,0),tmpSignClip.m_readingData[0].m_fPosition,tmpSignClip.m_readingData[1].m_fPosition);			
			signAcceData.SameValueInd.push_back(m_oriSignData.size()-1);
			SetAcceData(signAcceData,packageNum);
			
			SignClip signRssiData(ZeroAngle, ZeroAngle, Quaternion(0,0,0,0), Quaternion(0,0,0,0), Vector3::ZERO, Vector3::ZERO, tmpSignClip.m_readingData[LEFTt].m_fRssi, tmpSignClip.m_readingData[RIGHT].m_fRssi);
			signRssiData.SameValueInd.push_back(m_oriSignData.size()-1);
			SetRssiData(signRssiData, packageNum);
			/// other features add below
			
		}
		m_motionSeqLength.push_back(packageNum);
		std::cout << "motion length" << m_motionSeqLength.size()-1 << " = " << m_motionSeqLength[m_motionSeqLength.size()-1] << "\n";
		buildfp.close();
	}
	OSfp.close();
	OSfp.clear();
}

/**
*    @Function	SetSingData
*    @Brief		Set all feature data into m_signData and no same data in m_signData
*    @Param     signData is the sign clip want to push into m_signData
*/
void SignMotionData::SetSingData(SignClip signData,int packageNum)
{
	/*****check the raw value vector exist or not*****/
	/*If no the same motion clip ,then push it in to Sign_Data for encoding.*/
	bool isPush = true;
	for(int im = 0 ; im < m_signData.size() ;im++)
	{
		if(signData.Compare(m_signData[im]))  //same motion clip
		{
			//cout << "Got the same vector ... \n";
			isPush = false;
			m_signData[im].SameValueInd.push_back(m_oriSignData.size());
			break;
		}					
	}
	if(isPush)	
		m_signData.push_back(signData);
}


/**
*    @Function	SetSingData
*    @Brief		Set finger angle of all motion data into m_signFingerData and no same data in m_signFingerData
*    @Param     signFingerData is the sign clip only content finger angle want to push into m_signFingerData
*/
void SignMotionData::SetFingerData(SignClip signFingerData,int packageNum)
{
	/*****check the raw value vector exist or not*****/
	/*If no the same motion clip ,then push it in to Sign_Data for encoding.*/
	bool isPush = true;
	for(int im = 0 ; im < m_signFingerData.size() ;im++)
	{
		if(signFingerData.Compare(m_signFingerData[im].m_readingData[0].m_fFingerAngle,m_signFingerData[im].m_readingData[1].m_fFingerAngle))  //same motion clip
		{
			//cout << "Got the same vector ... \n";
			isPush = false;
			m_signFingerData[im].SameValueInd.push_back(m_oriSignData.size()-1);
			break;
		}					
	}
	if(isPush)
		m_signFingerData.push_back(signFingerData);
}

/**
*    @Function	SetQuaterData
*    @Brief		Set orientation of all motion data into m_signQuaterData and no same data in m_signQuaterData
*    @Param     signQuaterData is the sign clip only content orientation want to push into m_signQuaterData
*/
void SignMotionData::SetQuaterData(SignClip signQuaterData,int packageNum)
{
	/*****check the raw value vector exist or not*****/
	/*If no the same motion clip ,then push it in to Sign_Data for encoding.*/
	bool isPush = true;
	for(int im = 0 ; im < m_signQuaterData.size() ;im++)
	{
		if(signQuaterData.Compare(m_signQuaterData[im].m_readingData[0].m_qRotation,m_signQuaterData[im].m_readingData[1].m_qRotation))  //same motion clip
		{
			//cout << "Got the same vector ... \n";
			isPush = false;
			m_signQuaterData[im].SameValueInd.push_back( m_oriSignData.size()-1);
			break;
		}					
	}
	if(isPush)
		m_signQuaterData.push_back(signQuaterData);
}

/**
*    @Function	SetAcceData
*    @Brief		Set acceleration of all motion data into m_signAcceData and no same data in m_signAcceData
*	 @Param     signAcceData is the sign clip only content acceleration want to push into m_signAcceData
*/
void SignMotionData::SetAcceData(SignClip signAcceData,int packageNum)
{
	/*****check the raw value vector exist or not*****/
	/*If no the same motion clip ,then push it in to Sign_Data for encoding.*/
	bool isPush = true;
	for(int im = 0 ; im < m_signAcceData.size() ;im++)
	{
		if(signAcceData.Compare(m_signAcceData[im].m_readingData[0].m_fPosition,m_signAcceData[im].m_readingData[1].m_fPosition))  //same motion clip
		{
			//cout << "Got the same vector ... \n";
			isPush = false;
			m_signAcceData[im].SameValueInd.push_back(m_oriSignData.size()-1);
			break;
		}					
	}
	if(isPush)
		m_signAcceData.push_back(signAcceData);
}

void SignMotionData::SetRssiData(SignClip signRssiData,int packageNum)
{
    /*****check the raw value vector exist or not*****/
    /*If no the same motion clip ,then push it in to Sign_Data for encoding.*/
    bool isPush = true;
    for (int im = 0; im < m_signRssiData.size(); im++)
    {
        if (signRssiData.Compare(m_signRssiData[im].m_readingData[LEFTt].m_fRssi, m_signRssiData[im].m_readingData[RIGHT].m_fRssi))
        {
            isPush = false;
            m_signRssiData[im].SameValueInd.push_back(m_oriSignData.size() - 1);
            break;
        }
    }

    if (isPush)
    {
        m_signRssiData.push_back(signRssiData);
    }
}
/// other features add below

/**
*    @Function	SaveMotionDatabase
*    @Brief		Write Separate Feature to corresponding File
*	 @Param     database is the motion sequence
*    @Param     filename is the file name of database which is named by it feature
*/
void SignMotionData::SaveMotionDatabase(std::vector<SignClip> database,std::string filename)
{
	std::cout << "Data File :" << filename << " =>" << database.size() << "\n";

	char fileN[]="";
	strcpy(fileN, filename.c_str());
	
	//Remove original Data
	std::remove(fileN);
	std::fstream Sfp;
	Sfp.open(filename.c_str(), std::ios::out);
	for(int i = 0 ; i < database.size() ; i++)
	{
		SignClip tmpSignClip = database[i];
		Sfp << tmpSignClip.SignWordId << ","
			<< tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[8] << "," << tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[9] << "," 
			<< tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[6] << "," << tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[7] << ","
			<< tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[4] << "," << tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[5] << ","
			<< tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[2] << "," << tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[3] << ","
			<< tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[0] << "," << tmpSignClip.m_readingData[LEFTt].m_fFingerAngle[1] << ","
			<< tmpSignClip.m_readingData[LEFTt].m_qRotation.w     << "," << tmpSignClip.m_readingData[LEFTt].m_qRotation.x     << ","
			<< tmpSignClip.m_readingData[LEFTt].m_qRotation.y     << "," << tmpSignClip.m_readingData[LEFTt].m_qRotation.z     << ","
			<< tmpSignClip.m_readingData[LEFTt].m_fPosition.x     << "," << tmpSignClip.m_readingData[LEFTt].m_fPosition.y     << "," << tmpSignClip.m_readingData[0].m_fPosition.z << ","
			<< tmpSignClip.m_readingData[LEFTt].m_fRssi           << ","
			/// other features add below
			<< tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[8] << "," << tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[9] << "," 
			<< tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[6] << "," << tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[7] << ","
			<< tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[4] << "," << tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[5] << ","
			<< tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[2] << "," << tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[3] << ","
			<< tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[0] << "," << tmpSignClip.m_readingData[RIGHT].m_fFingerAngle[1] << ","
			<< tmpSignClip.m_readingData[RIGHT].m_qRotation.w     << "," << tmpSignClip.m_readingData[RIGHT].m_qRotation.x     << ","
			<< tmpSignClip.m_readingData[RIGHT].m_qRotation.y     << "," << tmpSignClip.m_readingData[RIGHT].m_qRotation.z     << ","
			<< tmpSignClip.m_readingData[RIGHT].m_fPosition.x     << "," << tmpSignClip.m_readingData[RIGHT].m_fPosition.y     << "," << tmpSignClip.m_readingData[1].m_fPosition.z << ","
			<< tmpSignClip.m_readingData[RIGHT].m_fRssi           << ",";
			/// other features add below
		
		for(int j=0;j<tmpSignClip.SameValueInd.size();j++)
		{
			Sfp << tmpSignClip.SameValueInd[j];
			if(j+1!=tmpSignClip.SameValueInd.size()) //check this data is end or not
				Sfp << ",";
		}

		Sfp << "\n";
	}
	
	Sfp.close();
	Sfp.clear();
}

/**
*    @Function	SaveMotionFileNameFile
*    @Brief		Save all Motion Data File Name in the file
*/
void SignMotionData::SaveMotionFileNameFile()
{
	//Remove original Data
	std::remove("..\\Data\\CGALDatabase\\Database\\SignName.csv");

	std::fstream SFNfp;
	SFNfp.open("..\\Data\\CGALDatabase\\Database\\SignName.csv", std::ios::out);
	for(int i = 0 ; i < m_signMotionFileName.size() ; i++)
	{
		SFNfp << m_signMotionFileName[i];
		if(i+1 < m_signMotionFileName.size())
			SFNfp << "\n";
	}
	SFNfp.close();
	SFNfp.clear();
}

/**
*    @Function	SaveMotionSeqLength
*    @Brief		Save Motion Data Sequence Length in the file
*/
void SignMotionData::SaveMotionSeqLength(){
	//Remove original Data
	std::remove("..\\Data\\CGALDatabase\\Database\\SignMotionLength.csv");

	std::fstream SMLfp;
	SMLfp.open("..\\Data\\CGALDatabase\\Database\\SignMotionLength.csv", std::ios::out);
	for(int i = 0 ; i < m_motionSeqLength.size() ; i++)
		SMLfp << m_motionSeqLength[i] << "\n";
	SMLfp.close();
	SMLfp.clear();
}

#include <android/log.h>

/**
*    @Function	LoadMotionDatabase
*    @Brief		Load Motion Data Progress
*/
void SignMotionData::LoadMotionDatabase()
{
	__android_log_print(ANDROID_LOG_DEBUG, "SignMotionData::LoadMotionDatabase", "Loading motion database...");
	
	m_oriSignData 	 = LoadFile("/sdcard/WearableDatabase/Database/OriSignData.csv");	
	m_signData       = LoadFile("/sdcard/WearableDatabase/Database/SignData.csv");
	m_signFingerData = LoadFile("/sdcard/WearableDatabase/Database/SignFingerData.csv");
	m_signQuaterData = LoadFile("/sdcard/WearableDatabase/Database/SignQuaterData.csv");
	m_signAcceData   = LoadFile("/sdcard/WearableDatabase/Database/SignAcceData.csv");
    m_signRssiData   = LoadFile("/sdcard/WearableDatabase/Database/SignRssiData.csv");
    /// other features add below
	
	__android_log_print(ANDROID_LOG_DEBUG, "SignMotionData::LoadMotionDatabase", "Loading motion database: Done");
	
	LoadMotionFileNameFile();
	__android_log_print(ANDROID_LOG_DEBUG, "SignMotionData::LoadMotionDatabase", "Loading file name file: Done");
	LoadMotionSeqLength();
	__android_log_print(ANDROID_LOG_DEBUG, "SignMotionData::LoadMotionDatabase", "Loading motion sequence length: Done");
}

/**
*    @Function	LoadFile
*    @Brief		Load Motion file into system
*    @Param     filename is the name of the motion file in the database
*/
std::vector<SignClip> SignMotionData::LoadFile(std::string filename)
{
	std::vector<SignClip> SignDatabase;

	std::fstream SignDatafp;
	std::string  str_buf;	
	{
		SignDatafp.open(filename.c_str(), std::fstream::in);

		if(!SignDatafp.is_open())
		{ 
//			std::cout << "File is not exist...\n";
			__android_log_print(ANDROID_LOG_ERROR, "SignMotionData::LoadFile", "File not exist: %s", filename.c_str());
			return SignDatabase;
		}

		while(!SignDatafp.eof())
		{
			getline(SignDatafp, str_buf); /* 先讀入一整行*/
			std::istringstream stream(str_buf); /* 使用istringstream */

			SignClip     tmpSignClip;
			float        arrL[DOF_SIGN], arrR[DOF_SIGN];
			int          streamLength = 0;

			/* 將 stream 內容, 以逗號分隔逐一讀取 */
			while (getline(stream, str_buf, ','))
			{
				if (streamLength == 0)
					tmpSignClip.SignWordId = atof(str_buf.c_str());
				else if (streamLength < DOF2_SIGN + 1)
				{
					if ((streamLength - 1) < DOF_SIGN)
						/* 用atoi 將字串轉成整數, 若要轉浮點數用atof */
						arrL[(streamLength - 1)] = atof(str_buf.c_str());
					else 
						/* 用atoi 將字串轉成整數, 若要轉浮點數用atof */
						arrR[(streamLength - 1) - DOF_SIGN] = atof(str_buf.c_str());
				}
				else
					tmpSignClip.SameValueInd.push_back(atof(str_buf.c_str()));
				streamLength++;
			}

			//left hand motion data		
			tmpSignClip.m_readingData[0].m_fFingerAngle[8] = arrL[0],tmpSignClip.m_readingData[0].m_fFingerAngle[9] = arrL[1];
			tmpSignClip.m_readingData[0].m_fFingerAngle[6] = arrL[2],tmpSignClip.m_readingData[0].m_fFingerAngle[7] = arrL[3];
			tmpSignClip.m_readingData[0].m_fFingerAngle[4] = arrL[4],tmpSignClip.m_readingData[0].m_fFingerAngle[5] = arrL[5];
			tmpSignClip.m_readingData[0].m_fFingerAngle[2] = arrL[6],tmpSignClip.m_readingData[0].m_fFingerAngle[3] = arrL[7];
			tmpSignClip.m_readingData[0].m_fFingerAngle[0] = arrL[8],tmpSignClip.m_readingData[0].m_fFingerAngle[1] = arrL[9];
			tmpSignClip.m_readingData[0].m_qRotation = Quaternion(arrL[10],arrL[11],arrL[12],arrL[13]);
			tmpSignClip.m_readingData[0].m_fPosition = Vector3(arrL[14],arrL[15],arrL[16]);
            tmpSignClip.m_readingData[LEFTt].m_fRssi = arrL[17];
            /// other features add below

			//right hand motion data
			tmpSignClip.m_readingData[1].m_fFingerAngle[8] = arrR[0],tmpSignClip.m_readingData[1].m_fFingerAngle[9] = arrR[1];
			tmpSignClip.m_readingData[1].m_fFingerAngle[6] = arrR[2],tmpSignClip.m_readingData[1].m_fFingerAngle[7] = arrR[3];
			tmpSignClip.m_readingData[1].m_fFingerAngle[4] = arrR[4],tmpSignClip.m_readingData[1].m_fFingerAngle[5] = arrR[5];
			tmpSignClip.m_readingData[1].m_fFingerAngle[2] = arrR[6],tmpSignClip.m_readingData[1].m_fFingerAngle[3] = arrR[7];
			tmpSignClip.m_readingData[1].m_fFingerAngle[0] = arrR[8],tmpSignClip.m_readingData[1].m_fFingerAngle[1] = arrR[9];
			tmpSignClip.m_readingData[1].m_qRotation = Quaternion(arrR[10],arrR[11],arrR[12],arrR[13]);
			tmpSignClip.m_readingData[1].m_fPosition = Vector3(arrR[14],arrR[15],arrR[16]);
            tmpSignClip.m_readingData[RIGHT].m_fRssi = arrR[17];
            /// other features add below
            
            //LogD("LoadFile", "L: %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f", arrL[0], arrL[1], arrL[2], arrL[3], arrL[4], arrL[5], arrL[6], arrL[7], arrL[8], arrL[9], arrL[10], arrL[11], arrL[12], arrL[13], arrL[14], arrL[15], arrL[16], arrL[17]);
            //LogD("LoadFile", "R: %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f", arrR[0], arrR[1], arrR[2], arrR[3], arrR[4], arrR[5], arrR[6], arrR[7], arrR[8], arrR[9], arrR[10], arrR[11], arrR[12], arrR[13], arrR[14], arrR[15], arrR[16], arrR[17]);
            
			SignDatabase.push_back(tmpSignClip);
			
//			__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::LOAD", "F: %s", filename.c_str());
//			__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::LOAD", "I: %d", tmpSignClip.SignWordId);
//			__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::LOAD", "L: %f %f %f %f", arrL[10],arrL[11],arrL[12],arrL[13]);
//			__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::LOAD", "R: %f %f %f %f", arrR[10],arrR[11],arrR[12],arrR[13]);
		}

		SignDatafp.close();
		SignDatafp.clear();
	}
	return SignDatabase;
}

/**
*    @Function	LoadFile
*    @Brief		Load Motion Data File Name
*/
void SignMotionData::LoadMotionFileNameFile(){
	//load Sign Motion Name
	{
		std::fstream SignDatafp;
		std::string str_buf;
		SignDatafp.open("/sdcard/WearableDatabase/Database/SignName.csv");

		if(!SignDatafp.is_open())
		{
			std::cout << "File is not exist...\n";
			__android_log_print(ANDROID_LOG_ERROR, "SignMotionData::LoadMotionFileNameFile", "File not exist.");
			return;
		}
		//first line of data
		while(!SignDatafp.eof())
		{
			getline(SignDatafp, str_buf); /* ��Ū�J�@���*/
			m_signMotionFileName.push_back(str_buf);
			__android_log_print(ANDROID_LOG_ERROR, "SignMotionData::LoadMotionFileNameFile", "%s", str_buf.c_str());
		}
		SignDatafp.close();
		SignDatafp.clear();
	}
}

/**
*    @Function	LoadMotionSeqLength
*    @Brief		Load each Motion Data Sequence Length
*/
void SignMotionData::LoadMotionSeqLength(){
	//load Sign Motion Length
	{
		m_maxLength = 0;
		int sumLength = 0;

		std::fstream SignDatafp;
		std::string str_buf;
		SignDatafp.open("/sdcard/WearableDatabase/Database/SignMotionLength.csv");
		if(!SignDatafp.is_open())
		{ 
			std::cout << "File is not exist...\n";
			__android_log_print(ANDROID_LOG_ERROR, "SignMotionData::LoadMotionSeqLength", "File not exist.");
			return;
		}
		
		while(!SignDatafp.eof())
		{
			getline(SignDatafp, str_buf); /* ��Ū�J�@���*/
			m_motionSeqLength.push_back(atoi(str_buf.c_str()));

			if(m_maxLength < m_motionSeqLength[m_motionSeqLength.size()-1])
				m_maxLength = m_motionSeqLength[m_motionSeqLength.size()-1];
			
			sumLength += m_motionSeqLength[m_motionSeqLength.size()-1];
			m_motionLengthSum.push_back(sumLength);
		}
		SignDatafp.close();
		SignDatafp.clear();
	}
}

/**
*    @Function	ClearDatabase
*    @Brief		*Clear memory of Database
*/
void SignMotionData::ClearDatabase()
{
	std::vector<SignClip>    tmp_signData;       
	std::vector<SignClip>    tmp_oriSignData;   
	std::vector<SignClip>    tmp_signFingerData;   
	std::vector<SignClip>    tmp_signQuaterData;   
	std::vector<SignClip>    tmp_signAcceData;
    std::vector<SignClip>    tmp_signRssiData;
    /// other features add below
	std::vector<int>         tmp_motionSeqLength;

	tmp_signData.swap(m_signData);
	tmp_oriSignData.swap(m_oriSignData);
	tmp_signFingerData.swap(m_signFingerData);
	tmp_signQuaterData.swap(m_signQuaterData);
	tmp_signAcceData.swap(m_signAcceData);
    tmp_signRssiData.swap(m_signRssiData);
    /// other features add below
	tmp_motionSeqLength.swap(m_motionSeqLength);
}
