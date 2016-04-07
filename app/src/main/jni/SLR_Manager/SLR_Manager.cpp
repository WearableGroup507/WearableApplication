/*
    SLR_Manager.cpp
    Author: Chin-Feng Chang
	Describe: Encoding Hand Feature separately by the data in the motion database  
			  Each data need to be integrate and difference to build KD-Tree       
*/

//------------------------------------------------------------------------------
// Includes
#include "SLR_Manager/SLR_Manager.h"
#include "EasyLog.h"

//------------------------------------------------------------------------------
// STL Methods
bool struct_cmp_by_engValue(const WE &a,const WE &b)
{
	return a.energyValue < b.energyValue;
}

bool struct_cmp_by_frameId(const WE &a,const WE &b)
{
	return a.frameId < b.frameId;
}


//------------------------------------------------------------------------------
// Class Methods

/************************************************************************/
/*                      Manager Member Function                         */
/************************************************************************/
/**
*    @Function	Init
*    @Brief		initialize SLR system include parameter , database encoding,
*               vocabulary weight computing,initialize optimization process.
*/
void SLR_Manager::Init()
{
	//parameter initialize
	m_IsRecognition = m_startRecognition = false;
	m_retrivelTime = 0;
	m_candidateWord = "";
	m_featureType = FeatureType::GLOVE;
	
	//preprocess
	SignEncodeing();

	//vocabulary weight setting
	InitialWeight();
	WeightNormalization();

	//For optimization process
	IsRecognition      = false;
	m_optimizationTime = 0;
	m_timeStep         = 0;
	m_lastTimeOutputWordId = "";
	InitTimelineBox();
}

/**
*    @Function	SignEncodeing
*    @Brief		call encoding function for each feature which save 
*               feature in the m_database
*/
void SLR_Manager::SignEncodeing()
{	
	LogD("SLR_Manager::SignEncoding", "Size: %d", m_database.m_signData.size());
	FingerEncoding();
	QuaterEncoding();
	AccelEncoding();
    RssiEncoding();
    /// other features add below
    
	//initial score vector
	int DatabaseSize = m_database.m_signMotionFileName.size();
	m_RetrievalWordBox.resize(DatabaseSize,0);
}

/**
*    @Function	SetFeatureType
*    @Brief		set the input feature type
*               0 : GLOVE
*               1 : WATCH
*               2 : ACC
*/
void SLR_Manager::SetFeatureType(std::string featureTypeCH)
{
	int featureTypeId = -1;
	if(featureTypeCH=="GLOVE") featureTypeId = 0;
	else if(featureTypeCH=="WATCH") featureTypeId = 1;
	else if(featureTypeCH=="ACC") featureTypeId = 2;	

	switch(featureTypeId)
	{
	case 0:
		m_featureType = FeatureType::GLOVE;
		std::cout << "Feature Type : " << featureTypeCH << "\n";
		break;
	case 1:
		m_featureType = FeatureType::WATCH;
		std::cout << "Feature Type : " << featureTypeCH << "\n";
		break;
	case 2:
		m_featureType = FeatureType::ACC;
		std::cout << "Feature Type : " << featureTypeCH << "\n";
		break;
	}
}
#include <Android/Log.h>
/**
*    @Function	RecognizeProcessing
*    @Brief		do motion retrieval by feature and optimization to find the sign language vocabulary
*    @Param     currMotionDataL is the motion feature data of left hand
*    @Param     currMotionDataR is the motion feature data of right hand
*/
void SLR_Manager::RecognizeProcessing(MotionDatastru currMotionDataL, MotionDatastru currMotionDataR)
{	
	MotionRetrieval(currMotionDataL._fingerAngle, currMotionDataR._fingerAngle);
	MotionRetrieval(currMotionDataL._rotation, currMotionDataR._rotation);
	MotionRetrieval(currMotionDataL._acc, currMotionDataR._acc);
    MotionRetrieval(currMotionDataL._rssi, currMotionDataR._rssi);
    /// other features add below
    
	//do optimization process as OLNG find the motion sequence path
//	__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::RecognizeProcessing", "%d, %d", m_quater_OLNG.m_getPath, m_accel_OLNG.m_getPath );
	if(m_quater_OLNG.m_getPath && m_accel_OLNG.m_getPath && m_rssi_OLNG.m_getPath)
    //if(m_rssi_OLNG.m_getPath)
	{
		/// other features add below
		GlobalOptimization();
	}
//	__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::RecognizeProcessing", "done");
}

/**
*    @Function	Reset
*    @Brief		reset the recognition process
*/
void SLR_Manager::Reset()
{
	m_IsRecognition = m_startRecognition = false;
	m_retrivelTime = 0;
	m_candidateWord = "";

	//reset parameter
	m_retrivelTime = 0;	
	m_optimizationTime = 0;

	//reset retrieval
	m_finger_OLNG.ClearGraph();
	m_quater_OLNG.ClearGraph();
	m_accel_OLNG.ClearGraph();
    m_rssi_OLNG.ClearGraph();
    /// other features add below

	//reset optimization
	ResetGlobalOptimization();
	for(int i = 0 ;i < m_RetrievalWordBox.size() ; i++)
		m_RetrievalWordBox[i] = 0;
}

/************************************************************************/
/*                          preprocess Encoding                         */
/************************************************************************/

/**
*    @Function	FingerEncoding
*    @Brief		assign finger angle data from m_database and build KD-Tree to encoding 
*/
void SLR_Manager::FingerEncoding(){
	/*create kd tree node storage*/
	fingerRealdata.resize(boost::extents[m_database.m_signFingerData.size()][DOF2_FINGER]); 
	//cout << "Num = " << Num << endl;

	/*assign data*/
	for (int i=0; i<m_database.m_signFingerData.size(); i++) {
		for (int j=0; j<10; j++) 
		{
			fingerRealdata[i][j]    = m_database.m_signFingerData[i].m_readingData[LEFTt].m_fFingerAngle[j];
			fingerRealdata[i][j+10] = m_database.m_signFingerData[i].m_readingData[RIGHT].m_fFingerAngle[j];
		}
	}

	LogD("SLR_Manager::FingerEncoding", "build finger tree ...");
	fingerTree = new kdtree2(fingerRealdata,false,DOF2_FINGER);
	LogD("SLR_Manager::FingerEncoding", "build finger tree complete!!...");
}

/**
*    @Function	QuaterEncoding
*    @Brief		assign orientation data from m_database and build KD-Tree to encoding
*/
void SLR_Manager::QuaterEncoding(){
	/*create kd tree node storage*/
	quaterRealdata.resize(boost::extents[m_database.m_signQuaterData.size()][DOF2_QUAT]); 
	//cout << "Num = " << Num << endl;

	/*assign data*/
	for (int i=0; i<m_database.m_signQuaterData.size(); i++) {
		quaterRealdata[i][0] = m_database.m_signQuaterData[i].m_readingData[LEFTt].m_qRotation.w;
		quaterRealdata[i][1] = m_database.m_signQuaterData[i].m_readingData[LEFTt].m_qRotation.x;
		quaterRealdata[i][2] = m_database.m_signQuaterData[i].m_readingData[LEFTt].m_qRotation.y;
		quaterRealdata[i][3] = m_database.m_signQuaterData[i].m_readingData[LEFTt].m_qRotation.z;


		quaterRealdata[i][4] = m_database.m_signQuaterData[i].m_readingData[RIGHT].m_qRotation.w;
		quaterRealdata[i][5] = m_database.m_signQuaterData[i].m_readingData[RIGHT].m_qRotation.x;
		quaterRealdata[i][6] = m_database.m_signQuaterData[i].m_readingData[RIGHT].m_qRotation.y;
		quaterRealdata[i][7] = m_database.m_signQuaterData[i].m_readingData[RIGHT].m_qRotation.z;
//		__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager", "quaterRealdata[i] = %f,%f,%f,%f,%f,%f,%f,%f", quaterRealdata[i][0], quaterRealdata[i][1], quaterRealdata[i][2], quaterRealdata[i][3], quaterRealdata[i][4], quaterRealdata[i][5], quaterRealdata[i][6], quaterRealdata[i][7]);
	}
	LogD("SLR_Manager::QuaterEncoding", "build Quaternion tree ...");
	quaterTree = new kdtree2(quaterRealdata,false,DOF2_QUAT);
	LogD("SLR_Manager::QuaterEncoding", "build Quaternion tree complete!! ...");	
}

/**
*    @Function	AccelEncoding
*    @Brief		assign acceleration data from m_database and build KD-Tree to encoding
*/
void SLR_Manager::AccelEncoding(){
	/*create kd tree node storage*/
	accelRealdata.resize(boost::extents[m_database.m_signAcceData.size()][DOF2_ACCE]); 
	//cout << "Num = " << Num << endl;

	/*assign data*/
	for (int i=0; i<m_database.m_signAcceData.size(); i++) {
		accelRealdata[i][0] = m_database.m_signAcceData[i].m_readingData[LEFTt].m_fPosition.x;
		accelRealdata[i][1] = m_database.m_signAcceData[i].m_readingData[LEFTt].m_fPosition.y;
		accelRealdata[i][2] = m_database.m_signAcceData[i].m_readingData[LEFTt].m_fPosition.z;

		accelRealdata[i][3] = m_database.m_signAcceData[i].m_readingData[RIGHT].m_fPosition.x;
		accelRealdata[i][4] = m_database.m_signAcceData[i].m_readingData[RIGHT].m_fPosition.y;
		accelRealdata[i][5] = m_database.m_signAcceData[i].m_readingData[RIGHT].m_fPosition.z;
	}
	LogD("SLR_Manager::AccelEncoding", "build Accelerator tree ...");
	accelTree = new kdtree2(accelRealdata,false,DOF2_ACCE);
	LogD("SLR_Manager::AccelEncoding", "build Accelerator tree complete!! ...");	
}

void SLR_Manager::RssiEncoding()
{
    /* Create kd tree node storage */
    rssiRealdata.resize(boost::extents[m_database.m_signRssiData.size()][DOF2_RSSI]);
    
    /* Assign data */
    for (int i=0; i < m_database.m_signRssiData.size(); i++)
    {
        rssiRealdata[i][0] = m_database.m_signRssiData[i].m_readingData[LEFTt].m_fRssi;
        rssiRealdata[i][1] = m_database.m_signRssiData[i].m_readingData[RIGHT].m_fRssi;
    }

    LogD("SLR_Manager::RssiEncoding", "build RSSI tree ...");
    rssiTree = new kdtree2(rssiRealdata, false, DOF2_RSSI);
    LogD("SLR_Manager::RssiEncoding", "build RSSI tree complete!! ...");
}
/// other features add below

/************************************************************************/
/*                         Vocabulary Weight                            */
/************************************************************************/

/**
*    @Function	InitialWeight
*    @Brief		set each motion data in the database to m_FeatureWeight 
                and calculate the vocabulary feature weight
*/
void SLR_Manager::InitialWeight()
{
	//resize the feature weight of motion
	m_FeatureWeight.resize(m_database.m_signMotionFileName.size());

	for(int i = 0 ; i < m_FeatureWeight.size() ; i++)
	{
		//get motion sequence
		int lowbound,upbound;

		if(i==0)
			lowbound = 0;
		else
			lowbound = m_database.m_motionLengthSum[i-1]+1;

		upbound = m_database.m_motionLengthSum[i];

		//std::cout << "get sequence:" << m_database.m_signMotionFileName[i] << " Range = (" << lowbound << "," << upbound << ")\n";
		std::vector<SignClip> tmpMotionSequence;
		for(int fid = lowbound ; fid <= upbound ; fid++)
			tmpMotionSequence.push_back(m_database.m_oriSignData[fid]);

		//set motion sequence and generate weight
		m_FeatureWeight[i].SetMotionSequence(tmpMotionSequence);
		m_FeatureWeight[i].GenWeight();

		std::vector<SignClip> emptyVector;
		tmpMotionSequence.swap(emptyVector);
	}
}

/**
*    @Function	WeightNormalization
*    @Brief		normalize the feature weight
*/
void SLR_Manager::WeightNormalization()
{
    float maxWfa, maxWori, maxWacc, maxWrssi;
    /// other features add below
    maxWfa = maxWori = maxWacc =  maxWrssi = 0.0f;
    /// other features add below

    float difWfa, difWori, difWacc, difWrssi;
    /// other features add below
    difWfa = difWori = difWacc = difWrssi = 0.0f;
    /// other features add below
	
	//取倒數並取最大值
	for(int i = 0 ; i < m_FeatureWeight.size() ; i++)
	{
        float newWfa, newWori, newWacc, newWrssi;
        /// other features add below
		newWfa = m_FeatureWeight[i].GetWfa();
		newWori = m_FeatureWeight[i].GetWori();
		newWacc = m_FeatureWeight[i].GetWacc();
        newWrssi = m_FeatureWeight[i].GetWrssi();
        /// other features add below

        float invWfa, invWori, invWacc, invWrssi;
        /// other features add below
		if (newWfa == 0)    invWfa = 0;     else invWfa = 1/newWfa;
		if (newWori ==0)    invWori = 0;    else invWori = 1/newWori;
		if (newWacc == 0)   invWacc = 0;    else invWacc = 1/newWacc;
        if (newWrssi == 0)  invWrssi = 0;   else invWrssi = 1/newWrssi;
        /// other features add below

        m_FeatureWeight[i].SetWeight(invWfa, invWori, invWacc, invWrssi);
        /// other features add below

		if (maxWfa < invWfa)    maxWfa = invWfa;
		if (maxWori < invWori)  maxWori = invWori;
		if (maxWacc < invWacc)  maxWacc = invWacc;
        if (maxWrssi < invWrssi) maxWrssi = invWrssi;
        /// other features add below
	}

	//總合與最大值得差值
	for(int i = 0 ; i < m_FeatureWeight.size() ; i++)
	{
		difWfa   += (maxWfa - m_FeatureWeight[i].GetWfa());
		difWori  += (maxWori - m_FeatureWeight[i].GetWori());
		difWacc  += (maxWacc - m_FeatureWeight[i].GetWacc());
        difWrssi += (maxWrssi - m_FeatureWeight[i].GetWrssi());
        /// other features add below
	}

	//除最大值上
	for(int i = 0 ; i < m_FeatureWeight.size() ; i++)
	{
        float newWfa, newWori, newWacc, newWrssi;
        /// other features add below
		newWfa   = (maxWfa - m_FeatureWeight[i].GetWfa()) / difWfa;
		newWori  = (maxWori - m_FeatureWeight[i].GetWori()) / difWori;
		newWacc  = (maxWacc - m_FeatureWeight[i].GetWacc()) / difWacc;
        newWrssi = (maxWrssi - m_FeatureWeight[i].GetWrssi() / difWrssi);
        /// other features add below
        m_FeatureWeight[i].SetWeight(newWfa+1, newWori+1, newWacc+1, newWrssi+1);
        /// other features add below
	}
}

/************************************************************************/
/*                          Retrieval process                           */
/*																		*/
/* 1. use input feature to do KNN search for similar motion             */
/* 2. pick the similar motion clip according to the word                */
/*    so classification the motion clip index by word                   */
/* 3. �ʺA�p��C�ӵ��J�ө�iOLNG���ƶq�A�B�̾�cost�j�p�M�w��J����      */
/************************************************************************/

/**
*    @Function	MotionRetrieval
*    @Brief		do the motion retrieval by OLNG according to input finger angle
*
*/
void SLR_Manager::MotionRetrieval(float _LfingerAngle[],float _RfingerAngle[])
{
	/************************************************************************/
	/*               �ھڿ�J����T��KNN search,�䵲�G��fingerResult         */
	/************************************************************************/
	std::vector<float> FingerQuery(DOF2_FINGER);
	fingerTree->sort_results = true;
	//Get Current Finger Angle Data for search
	for (int j=0; j<10; j++) 
	{
		FingerQuery[j]    = _LfingerAngle[j];
		FingerQuery[j+10] = _RfingerAngle[j];
	}	
	//Search Finger Angle Data in the Finger KD-Tree
	fingerTree->n_nearest(FingerQuery,KNN,fingerResult); // search for 10 nearest neighbor of query value and output to result
	

	/************************************************************************/
	/*               �qfingerResult�������X��y���J�P��۹�����frame id     */
	/************************************************************************/
	std::vector<std::vector<WE>>    candidateFrame;
	for(int KNNResultId =0 ; KNNResultId < fingerResult.size() ; KNNResultId++)
	{
		int SameValueFrameSize = m_database.m_signFingerData[fingerResult[KNNResultId].idx].SameValueInd.size();
		for(int SameValueFrameIdx = 0 ; SameValueFrameIdx < SameValueFrameSize ; SameValueFrameIdx++)
		{
			int FingerFrameOriIdx = m_database.m_signFingerData[fingerResult[KNNResultId].idx].SameValueInd[SameValueFrameIdx];
			int FingerSignWordId = m_database.m_oriSignData[FingerFrameOriIdx].SignWordId;

			//set the frame info
			WE frameInfo;
			frameInfo.wordId = FingerSignWordId;
			frameInfo.frameId = FingerFrameOriIdx;
			frameInfo.energyValue = fingerResult[KNNResultId].dis;

			//check the word is the candidate word or not.
			int SameWordId = -1; 
			for(int candidateFrameId = 0 ; candidateFrameId < candidateFrame.size() ; candidateFrameId++)
			{
				if(FingerSignWordId == candidateFrame[candidateFrameId][0].wordId)
					SameWordId = candidateFrameId;
			}


			if(SameWordId ==-1)
			{
				std::vector<WE> newCandidateFrameList;
				newCandidateFrameList.push_back(frameInfo);
				candidateFrame.push_back(newCandidateFrameList);
			}
			else
			{
				candidateFrame[SameWordId].push_back(frameInfo);
			}
		}
	}

	/************************************************************************/
	/*                    push candidate frame into OLNG                    */
	/************************************************************************/
	std::vector<OLNG_Node>    tmpSLNG_NodeVec;
	//����C�ӵ��J�ө�iOLNG
	int candidateWordSize = candidateFrame.size();
	if(candidateWordSize>0)
	{
		int avgFrameSizeForOLNG = m_finger_OLNG.K / candidateWordSize;  //�����C�ӵ����frame��
		while(1)
		{
			//��Xframe�Ƴ̱���alpha�����J
			int delta = 9999;
			int choosedWordId = -1;
			for(int wordIdx = 0 ; wordIdx < candidateFrame.size();wordIdx++)
			{
				int candidateFrameSize = candidateFrame[wordIdx].size();
				if(Math::Abs(candidateFrameSize-avgFrameSizeForOLNG)<delta && candidateFrame[wordIdx].size()!=0)
				{
					delta = Math::Abs(candidateFrameSize-avgFrameSizeForOLNG);
					choosedWordId = wordIdx;
				}
			}

			if(choosedWordId==-1) break;

			if(avgFrameSizeForOLNG > candidateFrame[choosedWordId].size()) avgFrameSizeForOLNG = candidateFrame[choosedWordId].size();

			for(int chooseFrameId = 0 ; chooseFrameId < avgFrameSizeForOLNG ; chooseFrameId++)
			{
				OLNG_Node tmpSLNG_Node;
				tmpSLNG_Node.SignClipInd = candidateFrame[choosedWordId][chooseFrameId].frameId;
				tmpSLNG_Node.nodeCost    = candidateFrame[choosedWordId][chooseFrameId].energyValue;
				tmpSLNG_NodeVec.push_back(tmpSLNG_Node);
			}
			candidateFrame[choosedWordId].clear();

			//�ʺA�p��C�ӵ��J�ө�iOLNG
			candidateWordSize--;
			if(candidateWordSize==0 || (m_finger_OLNG.K-tmpSLNG_NodeVec.size())==0)
				break;

			avgFrameSizeForOLNG = (m_finger_OLNG.K-tmpSLNG_NodeVec.size()) / candidateWordSize;
		}
		m_finger_OLNG.BuildGraph(tmpSLNG_NodeVec);
	}	
}

/**
*    @Function	MotionRetrieval
*    @Brief		do the motion retrieval by OLNG according to input orientation
*/
void SLR_Manager::MotionRetrieval(Quaternion _Lrotation,Quaternion _Rrotation)
{

	/************************************************************************/
	/*               �ھڿ�J����T��KNN search,�䵲�G��accelResult         */
	/************************************************************************/
	std::vector<float> QuaterQuery(DOF2_QUAT);
	quaterTree->sort_results = true;
	//Get Current Quaternion Data for search
	QuaterQuery[0] = _Lrotation.w;
	QuaterQuery[1] = _Lrotation.x;
	QuaterQuery[2] = _Lrotation.y;
	QuaterQuery[3] = _Lrotation.z;
	QuaterQuery[4] = _Rrotation.w;
	QuaterQuery[5] = _Rrotation.x;
	QuaterQuery[6] = _Rrotation.y;
	QuaterQuery[7] = _Rrotation.z;
	quaterTree->n_nearest(QuaterQuery,KNN,quaterResult); // search for 10 nearest neighbor of query value and output to result
	
	/************************************************************************/
	/*           �qquaterResult�������X��y���J�P��۹�����frame id         */
	/************************************************************************/
	std::vector<std::vector<WE>>    candidateFrame;
//	__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::MotionRetrieval", "ss: %s", m_candidateWord.c_str());
//	__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::MotionRetrieval", "%d", candidateFrame.size());
	for(int KNNResultId =0 ; KNNResultId < quaterResult.size() ; KNNResultId++)
	{
		int SameValueFrameSize = m_database.m_signQuaterData[quaterResult[KNNResultId].idx].SameValueInd.size();
		for(int SameValueFrameIdx = 0 ; SameValueFrameIdx < SameValueFrameSize ; SameValueFrameIdx++)
		{
			int QuaterFrameOriIdx = m_database.m_signQuaterData[quaterResult[KNNResultId].idx].SameValueInd[SameValueFrameIdx];
			int QuaterSignWordId = m_database.m_oriSignData[QuaterFrameOriIdx].SignWordId;

			//set the frame info
			WE frameInfo;
			frameInfo.wordId = QuaterSignWordId;
			frameInfo.frameId = QuaterFrameOriIdx;
			frameInfo.energyValue = quaterResult[KNNResultId].dis;

			//check the word is the candidate word or not.
			int SameWordId = -1; 
			for(int candidateFrameId = 0 ; candidateFrameId < candidateFrame.size() ; candidateFrameId++)
			{
				if(QuaterSignWordId == candidateFrame[candidateFrameId][0].wordId)
					SameWordId = candidateFrameId;
			}


			if(SameWordId ==-1)
			{
				std::vector<WE> newCandidateFrameList;
				newCandidateFrameList.push_back(frameInfo);
				candidateFrame.push_back(newCandidateFrameList);
			}
			else
			{
				candidateFrame[SameWordId].push_back(frameInfo);
			}
		}
	}

	/************************************************************************/
	/*                    push candidate frame into OLNG                    */
	/************************************************************************/
	std::vector<OLNG_Node>    tmpSLNG_NodeVec;
	//����C�ӵ��J�ө�iOLNG
	int candidateWordSize = candidateFrame.size();
	if(candidateWordSize > 0)
	{
		int avgFrameSizeForOLNG = m_quater_OLNG.K / candidateWordSize;  //�����C�ӵ����frame��
		while(1)
		{
			//��Xframe�Ƴ̱���alpha�����J
			int delta = 9999;
			int choosedWordId = -1;
			for(int wordIdx = 0 ; wordIdx < candidateFrame.size();wordIdx++)
			{
				int candidateFrameSize = candidateFrame[wordIdx].size();
				if(Math::Abs(candidateFrameSize-avgFrameSizeForOLNG)<delta && candidateFrame[wordIdx].size()!=0)
				{
					delta = Math::Abs(candidateFrameSize-avgFrameSizeForOLNG);
					choosedWordId = wordIdx;
				}
			}

			if(choosedWordId==-1) break;

			if(avgFrameSizeForOLNG > candidateFrame[choosedWordId].size()) avgFrameSizeForOLNG = candidateFrame[choosedWordId].size();

			for(int chooseFrameId = 0 ; chooseFrameId < avgFrameSizeForOLNG ; chooseFrameId++)
			{
				OLNG_Node tmpSLNG_Node;
				tmpSLNG_Node.SignClipInd = candidateFrame[choosedWordId][chooseFrameId].frameId;
				tmpSLNG_Node.nodeCost    = candidateFrame[choosedWordId][chooseFrameId].energyValue;
				tmpSLNG_NodeVec.push_back(tmpSLNG_Node);
			}
			candidateFrame[choosedWordId].clear();

			//�ʺA�p��C�ӵ��J�ө�iOLNG
			candidateWordSize--;
			if(candidateWordSize==0 || (m_quater_OLNG.K-tmpSLNG_NodeVec.size())==0)
				break;

			avgFrameSizeForOLNG = (m_quater_OLNG.K-tmpSLNG_NodeVec.size()) / candidateWordSize;
		}
//		__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::MotionRetrieval", "To build");
		m_quater_OLNG.BuildGraph(tmpSLNG_NodeVec);
	}	
	
}

/**
*    @Function	MotionRetrieval
*    @Brief		do the motion retrieval by OLNG according to input acceleration
*/
void SLR_Manager::MotionRetrieval(Vector3 _Lacc,Vector3 _Racc)
{
	/************************************************************************/
	/*               �ھڿ�J����T��KNN search,�䵲�G��accelResult         */
	/************************************************************************/
	std::vector<float> AccelQuery(DOF2_ACCE);
	accelTree->sort_results = true;
	//Get Current Accel Data for search
	AccelQuery[0] = _Lacc.x;
	AccelQuery[1] = _Lacc.y;
	AccelQuery[2] = _Lacc.z;
	AccelQuery[3] = _Racc.x;
	AccelQuery[4] = _Racc.y;
	AccelQuery[5] = _Racc.z;
	accelTree->n_nearest(AccelQuery,KNN,accelResult); // search for 10 nearest neighbor of query value and output to result

	/************************************************************************/
	/*               �qacclResult�������X��y���J�P��۹�����frame id       */
	/************************************************************************/
	std::vector<std::vector<WE>>    candidateFrame;
	for(int KNNResultId =0 ; KNNResultId < accelResult.size() ; KNNResultId++)
	{
		int SameValueFrameSize = m_database.m_signAcceData[accelResult[KNNResultId].idx].SameValueInd.size();
		for(int SameValueFrameIdx = 0 ; SameValueFrameIdx < SameValueFrameSize ; SameValueFrameIdx++)
		{
			int AccFrameOriIdx = m_database.m_signAcceData[accelResult[KNNResultId].idx].SameValueInd[SameValueFrameIdx];
			int AccSignWordId = m_database.m_oriSignData[AccFrameOriIdx].SignWordId;
			
			//set the frame info
			WE frameInfo;
			frameInfo.wordId = AccSignWordId;
			frameInfo.frameId = AccFrameOriIdx;
			frameInfo.energyValue = accelResult[KNNResultId].dis;

			//check the word is the candidate word or not.
			int SameWordId = -1; 
			for(int candidateFrameId = 0 ; candidateFrameId < candidateFrame.size() ; candidateFrameId++)
			{
				if(AccSignWordId == candidateFrame[candidateFrameId][0].wordId)
					SameWordId = candidateFrameId;
			}


			if(SameWordId ==-1)
			{
				std::vector<WE> newCandidateFrameList;
				newCandidateFrameList.push_back(frameInfo);
				candidateFrame.push_back(newCandidateFrameList);
			}
			else
			{
				candidateFrame[SameWordId].push_back(frameInfo);
			}
		}
	}

	/************************************************************************/
	/*                    push candidate frame into OLNG                    */
	/************************************************************************/
	std::vector<OLNG_Node>    tmpSLNG_NodeVec;
	//����C�ӵ��J�ө�iOLNG
	int candidateWordSize = candidateFrame.size();
	if(candidateWordSize > 0)
	{
		int avgFrameSizeForOLNG = m_accel_OLNG.K / candidateWordSize;  //�����C�ӵ����frame��
		while(1)
		{
			//��Xframe�Ƴ̱���alpha�����J
			int delta = 9999;
			int choosedWordId = -1;
			for(int wordIdx = 0 ; wordIdx < candidateFrame.size();wordIdx++)
			{
				int candidateFrameSize = candidateFrame[wordIdx].size();
				if(Math::Abs(candidateFrameSize-avgFrameSizeForOLNG)<delta && candidateFrame[wordIdx].size()!=0)
				{
					delta = Math::Abs(candidateFrameSize-avgFrameSizeForOLNG);
					choosedWordId = wordIdx;
				}
			}

			if(choosedWordId==-1) break;

			if(avgFrameSizeForOLNG > candidateFrame[choosedWordId].size()) avgFrameSizeForOLNG = candidateFrame[choosedWordId].size();

			for(int chooseFrameId = 0 ; chooseFrameId < avgFrameSizeForOLNG ; chooseFrameId++)
			{
				OLNG_Node tmpSLNG_Node;
				tmpSLNG_Node.SignClipInd = candidateFrame[choosedWordId][chooseFrameId].frameId;
				tmpSLNG_Node.nodeCost    = candidateFrame[choosedWordId][chooseFrameId].energyValue;
				tmpSLNG_NodeVec.push_back(tmpSLNG_Node);
			}
			candidateFrame[choosedWordId].clear();

			//�ʺA�p��C�ӵ��J�ө�iOLNG
			candidateWordSize--;
			if(candidateWordSize==0 || (m_accel_OLNG.K-tmpSLNG_NodeVec.size())==0)
				break;

			avgFrameSizeForOLNG = (m_accel_OLNG.K-tmpSLNG_NodeVec.size()) / candidateWordSize;
		}
		m_accel_OLNG.BuildGraph(tmpSLNG_NodeVec);
	}
}

void SLR_Manager::MotionRetrieval(float _Lrssi, float _Rrssi)
{
    std::vector<float> RssiQuery(DOF2_RSSI);
    rssiTree->sort_results = true;

    // Get current RSSI data for search
    RssiQuery[0] = _Lrssi;
    RssiQuery[1] = _Rrssi;
    rssiTree->n_nearest(RssiQuery, KNN, rssiResult);

    // Categorize the sign word and corresponding frame id form query result
    std::vector<std::vector<WE>> candidateFrame;
    for (int KNNResultId = 0; KNNResultId < rssiResult.size(); KNNResultId++)
    {
        int SameValueFrameSize = m_database.m_signRssiData[rssiResult[KNNResultId].idx].SameValueInd.size();
        for (int SameValueFrameIdx = 0; SameValueFrameIdx < SameValueFrameSize; SameValueFrameIdx++)
        {
            int RssiFrameOriIdx = m_database.m_signRssiData[rssiResult[KNNResultId].idx].SameValueInd[SameValueFrameIdx];
            int RssiSignWordId = m_database.m_oriSignData[RssiFrameOriIdx].SignWordId;

            // Set the frame info
            WE frameInfo;
            frameInfo.wordId = RssiSignWordId;
            frameInfo.frameId = RssiFrameOriIdx;
            frameInfo.energyValue = rssiResult[KNNResultId].dis;
            
            //LogD("MotionRet", "%f, %f, %f, %d, %d", _Lrssi, _Rrssi, frameInfo.energyValue, frameInfo.wordId, frameInfo.frameId);

            // Check the word is the candidate word or not
            int SameWordId = -1;
            for (int candidateFrameId = 0; candidateFrameId < candidateFrame.size(); candidateFrameId++)
            {
                if (RssiSignWordId == candidateFrame[candidateFrameId][0].wordId)
                    SameWordId = candidateFrameId;
            }
            
            if (SameWordId == -1)
            {
                std::vector<WE> newCandidateFrameList;
                newCandidateFrameList.push_back(frameInfo);
                candidateFrame.push_back(newCandidateFrameList);
            }
            else
            {
                candidateFrame[SameWordId].push_back(frameInfo);
            }
        }
    }

    // Push candidate frame into OLNG
    std::vector<OLNG_Node> tmpOLNG_NodeVec;
    int candidateWordSize = candidateFrame.size();
    if (candidateWordSize > 0)
    {
        int avgFrameSizeForOLNG = m_rssi_OLNG.K / candidateWordSize;
        while (true)
        {
            int delta = (std::numeric_limits<int>::max)();
            int chooseWordId = -1;
            for (int wordId = 0; wordId < candidateFrame.size(); wordId++)
            {
                int candidateFrameSize = candidateFrame[wordId].size();
                if (Math::Abs(candidateFrameSize - avgFrameSizeForOLNG) < delta && candidateFrame[wordId].size() != 0)
                {
                    delta = Math::Abs(candidateFrameSize - avgFrameSizeForOLNG);
                    chooseWordId = wordId;
                }
            }

            if (chooseWordId == -1) break;

            if (avgFrameSizeForOLNG > candidateFrame[chooseWordId].size())
                avgFrameSizeForOLNG = candidateFrame[chooseWordId].size();

            for (int chooseFrameId = 0; chooseFrameId < avgFrameSizeForOLNG; chooseFrameId++)
            {
                OLNG_Node tmpOLNG_Node;
                tmpOLNG_Node.SignClipInd    = candidateFrame[chooseWordId][chooseFrameId].frameId;
                tmpOLNG_Node.nodeCost       = candidateFrame[chooseWordId][chooseFrameId].energyValue;
                tmpOLNG_NodeVec.push_back(tmpOLNG_Node);
            }

            candidateWordSize--;
            if (candidateWordSize == 0 || (m_rssi_OLNG.K - tmpOLNG_NodeVec.size()) == 0)
                break;

            avgFrameSizeForOLNG = (m_rssi_OLNG.K - tmpOLNG_NodeVec.size()) / candidateWordSize;
        }

        m_rssi_OLNG.BuildGraph(tmpOLNG_NodeVec);
    }
}
/// other features add below


/************************************************************************/
/*                           Optimization                               */
/************************************************************************/

/**
*    @Function	GlobalOptimization
*    @Brief		combine all information from local optimization and find out 
*               the most similar vocabulary
*
*				Step 1. �ھڿ�J��T�@�ɶ��It��local optimization 
*                       �æ^�ǸӮɶ��I�i�઺frame�ά۹�����word info localOpResult
*				Step 2. ��sm_candidateWord_Final
*                       �ˬd�ɶ��It�ҧ�X�Ӫ�candidate word�O�_�s�b��m_candidateWord_Final
*                       �Y�S���h�s�W
*				Step 3. �ھ�frame index �� localOpResult�@�Ƨ�
*				Step 4. �NlocalOpResult����T�ھڵ��J�N����ΥX�ӼȦs��localOpWord
*                       �A��localOpWord�ھ�cost�Ƨǫ�A��X�e���ӳ̤p��frame��JpickedFrame
*				Step 5. ��sm_GlobalMotionGraph_Final
*						�p�Ggraph���S��candidate word,�h�Npicked frame���C�ӵ��J�̤pcost frame��J
*						�p�G�A�ɶ��It-1�����ۦP�����J,�h�ھڵ��J��motion sequence index�j�p��J�̱���index frame
*						�I�sUpdateTimeline,�b�C��time interval�ɶ���ɡA��X���G
*/
void SLR_Manager::GlobalOptimization()
{
//	m_fp.open("C:\\Users\\chinfeng\\Desktop\\Wearable Sign Language Interpretation System\\SignLanage\\Data\\Log\\Global_Optimization_Log.txt", std::ios::out | std::ios::app);

	int K = m_finger_OLNG.K;
	int M = m_finger_OLNG.M;
	

// 	m_fp << "============================OLNG Output=============================\n";
// 	for(int i = 0 ; i < K ; i++)
// 	{
// 		float clipId[4];
// 		float wordId[4];
// 		std::string wordString[4];
// 	
// 		clipId[0] = m_finger_OLNG.m_Graph[M-1][i].SignClipInd;
// 		wordId[0] = m_database.m_oriSignData[clipId[0]].SignWordId;
// 		wordString[0] = m_database.m_signMotionFileName[wordId[0]];
// 		 
// 		clipId[1] = m_quater_OLNG.m_Graph[M-1][i].SignClipInd;
// 		wordId[1] = m_database.m_oriSignData[clipId[1]].SignWordId;
// 		wordString[1] = m_database.m_signMotionFileName[wordId[1]];
// 	
// 		clipId[2] = m_accel_OLNG.m_Graph[M-1][i].SignClipInd;
// 		wordId[2] = m_database.m_oriSignData[clipId[2]].SignWordId;
// 		wordString[2] = m_database.m_signMotionFileName[wordId[2]];
// 	
// 		m_fp << wordString[0] << " " << clipId[0] << " " << m_finger_OLNG.m_finalCost[i] << " "
// 			<< wordString[1] << " " << clipId[1] << " " << m_quater_OLNG.m_finalCost[i] << " "
// 			<< wordString[2] << " " << clipId[2] << " " << m_accel_OLNG.m_finalCost[i] << "\n";
// 	}
	// 	m_fp << "Minimum Cost = "<< m_finger_OLNG.m_minimumFinalCost << " " << m_quater_OLNG.m_minimumFinalCost << " " << m_accel_OLNG.m_minimumFinalCost << "\n";
	// 	m_fp << "Penalty Cost Sum in OLNG = "<< m_finger_OLNG.m_penaltySum << " " << m_quater_OLNG.m_penaltySum << " " << m_accel_OLNG.m_penaltySum << "\n";

	//do local optimization at time t
	std::vector<WE> localOpResult;
	switch(m_featureType)
	{
	case FeatureType::GLOVE:
		localOpResult = LocalOptimization();
		//localOpResult = RssiLocalOptimization();
		break;
	case FeatureType::WATCH:
		localOpResult = IMUsLocalOptimization();
//		__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::GlobalOptimization", "WATCH");
		break;
	case FeatureType::ACC:
		localOpResult = ACCLocalOptimization();
		break;
	}
	
	//	m_fp << "============================local candidate word=============================\n";
	for(int i = 0 ; i < m_candidateWord_local.size() ; i++)	
	{
		//m_fp <<  m_database.m_signMotionFileName[m_candidateWord_local[i]]<<"\n";
		
		//if the word not exist in the m_candidateWord_Final, then add in.
		bool wordExist = false;
		for(int j = 0 ; j < m_candidateWord_Final.size() ; j++)
		{
			if(m_candidateWord_Final[j].wordId == m_candidateWord_local[i])
			{
				wordExist = true;
				break;
			}
		}
		
		if(!wordExist) 
		{
			WE newCandidateWord;
			newCandidateWord.wordId = m_candidateWord_local[i];
			newCandidateWord.showNum = 0;
			newCandidateWord.frameId = 0;
			newCandidateWord.energyValue = 0;
			m_candidateWord_Final.push_back(newCandidateWord);
			m_TimeIntervalBox.push_back(newCandidateWord);
		}
	}
	//	m_fp << "--------------------------------------------------------------------------------\n";

	std::sort(localOpResult.begin(),localOpResult.end(), struct_cmp_by_frameId); 	
	// 	m_fp << "========================Sort by frame Local Optimization=========================\n";
	// 	for(int i = 0 ; i < localOpResult.size() ; i++)
	// 	{
	// 		m_fp << m_database.m_signMotionFileName[localOpResult[i].wordId] <<" " << localOpResult[i].frameId << " " << localOpResult[i].energyValue << "\n";
	// 	}

	//m_fp << "========================Sort by Value Local Optimization=========================\n";
	int currWordId = -1;
	std::vector<WE>  localOpWord;
	std::vector<std::vector<WE>>  pickedFrame;
	for(int i = 0 ; i < localOpResult.size() ; i++)
	{
		//std::cout << "localOpResult word Id:" << localOpResult[i].wordId << " current word Id:" << currWordId  << "\n";
		if(currWordId==-1)
		{
			currWordId=localOpResult[0].wordId;
			localOpWord.push_back(localOpResult[i]);
		}
		else if(currWordId==localOpResult[i].wordId)
			localOpWord.push_back(localOpResult[i]);
		else if(currWordId!=localOpResult[i].wordId)
		{	
			std::sort(localOpWord.begin(),localOpWord.end(), struct_cmp_by_engValue);

			std::vector<WE> tmpPickedFrame;
			for(int i = 0 ; i < localOpWord.size() ; i++)
			{
				//m_fp << m_database.m_signMotionFileName[localOpWord[i].wordId] <<" " << localOpWord[i].frameId << " " << localOpWord[i].energyValue << "\n";
				if(i<5)
					tmpPickedFrame.push_back(localOpWord[i]);
			}
			//m_fp << "=========================================================================\n";
			pickedFrame.push_back(tmpPickedFrame);
			std::vector<WE>  tmplocalOpWord;
			tmplocalOpWord.swap(localOpWord);
			tmplocalOpWord.swap(tmpPickedFrame);

			currWordId = localOpResult[i].wordId;
			localOpWord.push_back(localOpResult[i]);
		}
	}

	if(localOpWord.size()!=0)
	{
		std::vector<WE> tmpPickedFrame;
		std::sort(localOpWord.begin(),localOpWord.end(), struct_cmp_by_engValue);
		for(int i = 0 ; i < localOpWord.size() ; i++)
		{
			tmpPickedFrame.push_back(localOpWord[i]);
		}
		//m_fp << "=========================================================================\n";
		pickedFrame.push_back(tmpPickedFrame);
		std::vector<WE>  tmplocalOpWord;
		tmplocalOpWord.swap(localOpWord);
		tmplocalOpWord.swap(tmpPickedFrame);
	}

	/************************************************************************/
	/*                         Connect with last frame                      */
	/************************************************************************/
	if(m_timeStep ==0 && pickedFrame.size()>0)
	{
		//push the smallest frame into global motion graph
		for(int wid = 0 ; wid < pickedFrame.size() ; wid++)
		{
			std::vector<WE> pf = pickedFrame[wid];
			std::sort(pf.begin(),pf.end(), struct_cmp_by_frameId);
			m_GlobalMotionGraph_Final[m_timeStep].push_back(pf[0]);
		}
	}
	else if(m_timeStep!=0)
	{
		if(m_GlobalMotionGraph_Final[m_timeStep-1].size()==0 && pickedFrame.size()>0)
		{
			//push the smallest frame into global motion graph
			for(int wid = 0 ; wid < pickedFrame.size() ; wid++)
			{
				std::vector<WE> pf = pickedFrame[wid];				
				std::sort(pf.begin(),pf.end(), struct_cmp_by_frameId);
				m_GlobalMotionGraph_Final[m_timeStep].push_back(pf[0]);
			}
		}
		else if(pickedFrame.size()>0)
		{
			for(int wid = 0 ; wid < pickedFrame.size() ; wid++)
			{
				std::vector<WE> pf = pickedFrame[wid];				
				std::sort(pf.begin(),pf.end(), struct_cmp_by_frameId);

				//check the last time has the same candidate word or not
				int sameWordId = -1;
				for(int t_1Id = 0 ; t_1Id < m_GlobalMotionGraph_Final[m_timeStep-1].size() ; t_1Id++)
				{
					if(m_GlobalMotionGraph_Final[m_timeStep-1][t_1Id].wordId == pf[0].wordId)
					{
						sameWordId = t_1Id;
						break;
					}
				}

				//have no same word at last time step
				if(sameWordId==-1)
				{
					m_GlobalMotionGraph_Final[m_timeStep].push_back(pf[0]);
				}
				//have same word at last time step
				else
				{
					//so find the frameId �j�� t_1Id's frameId and frameId different less than 4
					bool hasBigger = false,hasEqualOne = false;
					int  equareOneIdx = -1;
					int  LastOneIdx = -1;
					for(int pfIdx = 0 ; pfIdx < pf.size() ; pfIdx++)
					{
						if(pf[pfIdx].frameId > m_GlobalMotionGraph_Final[m_timeStep-1][sameWordId].frameId && (pf[pfIdx].frameId - m_GlobalMotionGraph_Final[m_timeStep-1][sameWordId].frameId) <=4)
						{
							m_GlobalMotionGraph_Final[m_timeStep].push_back(pf[pfIdx]);
							hasBigger = true;
							break;
						}

						if(pf[pfIdx].frameId == m_GlobalMotionGraph_Final[m_timeStep-1][sameWordId].frameId)
						{
							equareOneIdx = pfIdx;
							hasEqualOne = true;
						}

						if((pf[pfIdx].frameId-1 == m_GlobalMotionGraph_Final[m_timeStep-1][sameWordId].frameId))
						{
							LastOneIdx = pfIdx;
						}
					}

					//�p�G�䤣���L�j�����O���۵����~��i�۵���frame
					if(!hasBigger && equareOneIdx!=-1)  m_GlobalMotionGraph_Final[m_timeStep].push_back(pf[equareOneIdx]);
					else if(!hasBigger && !hasEqualOne && LastOneIdx!=-1)  m_GlobalMotionGraph_Final[m_timeStep].push_back(pf[LastOneIdx]);
				}
			}
		}
	}

	UpdateTimelineBox();

	if(m_fp.is_open())    {m_fp.close();    m_fp.clear();  }
	m_optimizationTime++;
	m_timeStep++;	
}

/**
*    @Function	ResetGlobalOptimization
*    @Brief		reset the global optimization
*/
void SLR_Manager::ResetGlobalOptimization()
{
	m_timeStep = 0;

	std::vector<WE> tmpWord;
	tmpWord.swap(m_candidateWord_Final);
}

/**
*    @Function	LocalOptimization
*    @Brief		�ĦX3��OLNG�Ϫ���T�@�ɶ��It��local optimization
*               Step 1. compute cost weight
*				Step 2. �ھ�OLNG��X�����G��z�P�ɥX�{�b�T�ӹϩζȥX�{�bFinger OLNG�MOri OLNG���r
*                        ��Jm_candidateWord_local
*				Step 3. �ھڲξ�X�Ӫ��r���U��OLNG�Ϥ���frame�Ȧs��frameSet�Ω�p��optimization energy value
*				Step 4. �̷�error term�p��ƭȧ@�̨Τ�
*    @Return    the vector content all frame from OLNG result with optimize energy at time t
*/
std::vector<WE> SLR_Manager::LocalOptimization()
{
	/************************************************************************/
	/*                              Parameter                               */
	/************************************************************************/
	std::vector<WE> localOptResult;
	int K = m_finger_OLNG.K , M = m_finger_OLNG.M;


	//設定threshold以過濾掉過大的cost ==> 沒有參考價值得path
    float OLNGCostThreshold[NUM_FEATURE] = {0.0f}; // clear values to zero

	//Method 2 取final cost的平均
	for(int i = 0 ; i < K; i++)
	{
		OLNGCostThreshold[0] += m_finger_OLNG.m_finalCost[i];
		OLNGCostThreshold[1] += m_quater_OLNG.m_finalCost[i];
		OLNGCostThreshold[2] += m_accel_OLNG.m_finalCost[i];
        OLNGCostThreshold[3] += m_rssi_OLNG.m_finalCost[i];
        /// other features add below
	}
// 	OLNGCostThreshold[0] /= (K-10);
// 	OLNGCostThreshold[1] /= (K-10);
// 	OLNGCostThreshold[2] /= (K-10);

	/************************************************************************/
	/*                         Calculate cost weight                        */
	/************************************************************************/
	//step 1. find the maximum cost
	float maxCost[NUM_FEATURE] = {0.0f}; // clear values to zero
	for(int i = 0 ; i < K ; i++)
	{
		if(maxCost[0] < m_finger_OLNG.m_finalCost[i] && m_finger_OLNG.m_finalCost[i] < OLNGCostThreshold[0])
			maxCost[0] = m_finger_OLNG.m_finalCost[i];

		if(maxCost[1] < m_quater_OLNG.m_finalCost[i] && m_quater_OLNG.m_finalCost[i] < OLNGCostThreshold[1])
			maxCost[1] = m_quater_OLNG.m_finalCost[i];

		if(maxCost[2] < m_accel_OLNG.m_finalCost[i] && m_accel_OLNG.m_finalCost[i] < OLNGCostThreshold[2])
			maxCost[2] = m_accel_OLNG.m_finalCost[i];

        if(maxCost[3] < m_rssi_OLNG.m_finalCost[i] && m_rssi_OLNG.m_finalCost[i] < OLNGCostThreshold[3])
            maxCost[3] = m_rssi_OLNG.m_finalCost[i];
        /// other features add below
	}

	//step 2. sum all cost
	float deltaSum[NUM_FEATURE] = {0.0f};
	for(int i = 0 ; i < K ; i++)
	{
		if(m_finger_OLNG.m_finalCost[i] < OLNGCostThreshold[0])
			deltaSum[0] += (maxCost[0] - m_finger_OLNG.m_finalCost[i]);
		if(m_quater_OLNG.m_finalCost[i] < OLNGCostThreshold[1])
			deltaSum[1] += (maxCost[1] - m_quater_OLNG.m_finalCost[i]);
		if(m_accel_OLNG.m_finalCost[i] < OLNGCostThreshold[2])
			deltaSum[2] += (maxCost[2] - m_accel_OLNG.m_finalCost[i]);
        if(m_rssi_OLNG.m_finalCost[i] < OLNGCostThreshold[3])
            deltaSum[3] += (maxCost[3] - m_rssi_OLNG.m_finalCost[i]);
        /// other features add below
	}
	//std::cout << "Delta Sum cost :" << deltaSum[0] << " " << deltaSum[1] << " " << deltaSum[2] << "\n";
	//step 3. Wi = (maxCost - cost[i])/deltaSum;
	//float CostWeight[3][40];
	float **CostWeight;
	CostWeight = new float*[NUM_FEATURE];
    for (int i = 0; i < NUM_FEATURE; i++)
    {
        CostWeight[i] = new float[K];
    }

	//m_fp << "==============================OLNG Cost ===============================\n";
	for(int i = 0 ; i < K ; i++)
	{
		//Finger angle
		if(m_finger_OLNG.m_finalCost[i] < OLNGCostThreshold[0])
			CostWeight[0][i] = (maxCost[0] - m_finger_OLNG.m_finalCost[i])/deltaSum[0];
		else 
			CostWeight[0][i] = 0;

		//Orientation
		if(m_quater_OLNG.m_finalCost[i] < OLNGCostThreshold[1])
			CostWeight[1][i] = (maxCost[1] - m_quater_OLNG.m_finalCost[i])/deltaSum[1];
		else 
			CostWeight[1][i] = 0;

		//Acceleration
		if(m_accel_OLNG.m_finalCost[i] < OLNGCostThreshold[2])
			CostWeight[2][i] = (maxCost[2] - m_accel_OLNG.m_finalCost[i])/deltaSum[2];
		else
			CostWeight[2][i] = 0;

        //RSSI
        if(m_rssi_OLNG.m_finalCost[i] < OLNGCostThreshold[3])
            CostWeight[3][i] = (maxCost[3] - m_rssi_OLNG.m_finalCost[i])/deltaSum[3];
        /// other features add below
        else
            CostWeight[3][i] = 0;
		//m_fp << "cost weight : " << i << " " << CostWeight[0][i] << " " << CostWeight[1][i] << " " << CostWeight[2][i] << "\n";
	}
	//m_fp << "==========================================================================\n";

	/************************************************************************/
	/*                            Get OLNG Frame                            */
	/************************************************************************/
	std::vector<std::vector<WE>> frameSet;
	std::vector<int>  WordIdSet;
	std::vector<bool> FingerWord;
	std::vector<bool> QuaterWord;
	std::vector<bool> AccWord;
    std::vector<bool> RssiWord;
    /// other features add below
	int len = m_database.m_signMotionFileName.size();
	FingerWord.resize(len, false);
	QuaterWord.resize(len, false);
	AccWord.resize(len, false);
    RssiWord.resize(len, false);
    /// other features add below
	std::vector<bool> frameInputCheck;
	frameInputCheck.resize(m_database.m_oriSignData.size(),false);
	
	for(int idx = 0 ; idx < K ; idx++)
	{
		//Finger angle
		if(CostWeight[0][idx]!=0) //捨去過濾掉的frame
		{
			int FingerframeId = m_finger_OLNG.m_Graph[m_finger_OLNG.M-1][idx].SignClipInd;
			int FingerwordId  = m_database.m_oriSignData[FingerframeId].SignWordId;
			FingerWord[FingerwordId] = true;
		}

		//Orientation
		if(CostWeight[1][idx]!=0) //捨去過濾掉的frame
		{
			int OriframeId = m_quater_OLNG.m_Graph[m_quater_OLNG.M-1][idx].SignClipInd;
			int OriwordId  = m_database.m_oriSignData[OriframeId].SignWordId;
			QuaterWord[OriwordId] = true;
		}

		//Acceleration
		if(CostWeight[2][idx]!=0) //捨去過濾掉的frame
		{
			int AccframeId = m_accel_OLNG.m_Graph[m_accel_OLNG.M-1][idx].SignClipInd;
			int AccwordId  = m_database.m_oriSignData[AccframeId].SignWordId;
			AccWord[AccwordId] = true;
		}
		
        //RSSI
        if(CostWeight[3][idx]!=0) //捨去過濾掉的frame
        {
            int RssiframeId = m_rssi_OLNG.m_Graph[m_rssi_OLNG.M-1][idx].SignClipInd;
            int RssiwordId  = m_database.m_oriSignData[RssiframeId].SignWordId;
            RssiWord[RssiwordId] = true;
        }
        /// other features add below
	}
	
	//clear local candidate word
	std::vector<int> emplocalword;
	emplocalword.swap(m_candidateWord_local);

	//找出OLNG圖結果中相同的字詞
	for(int idx = 0 ; idx < len ; idx++)
	{	
		if((FingerWord[idx] && QuaterWord[idx]) || (FingerWord[idx] && AccWord[idx]) || (QuaterWord[idx] && AccWord[idx]))
        //if((FingerWord[idx] && QuaterWord[idx] && AccWord[idx] && RssiWord[idx]) || (FingerWord[idx] && QuaterWord[idx] && RssiWord[idx]))
		{
            /// other features add below
			WordIdSet.push_back(idx);
			m_candidateWord_local.push_back(idx);
		}
	}
	
	frameSet.resize(WordIdSet.size());

	for(int idx = 0 ; idx < K ; idx++)
	{
		//Finger angle
		int FingerframeId = m_finger_OLNG.m_Graph[m_finger_OLNG.M-1][idx].SignClipInd;
		int FingerwordId  = m_database.m_oriSignData[FingerframeId].SignWordId;
		//Orientation
		int OriframeId = m_quater_OLNG.m_Graph[m_quater_OLNG.M-1][idx].SignClipInd;
		int OriwordId  = m_database.m_oriSignData[OriframeId].SignWordId;
		//Acceleration
		int AccframeId = m_accel_OLNG.m_Graph[m_accel_OLNG.M-1][idx].SignClipInd;
		int AccwordId  = m_database.m_oriSignData[AccframeId].SignWordId;
        //RSSI
        int RssiframeId = m_rssi_OLNG.m_Graph[m_rssi_OLNG.M-1][idx].SignClipInd;
        int RssiwordId  = m_database.m_oriSignData[RssiframeId].SignWordId;
        /// other features add below
        
		//check the word is already in the candidate word or not.
		//if the word exist, then update the boundary.
		//放入不重複的frame到frameSet準備做計算
		WE frameInfo;		
		for(int j=0;j<WordIdSet.size();j++)
		{
			frameInfo.wordId = WordIdSet[j];
			if(WordIdSet[j]==FingerwordId && CostWeight[0][idx]!=0)
			{
				frameInfo.frameId = FingerframeId;
				if(!frameInputCheck[frameInfo.frameId])
				{
					frameSet[j].push_back(frameInfo);
					frameInputCheck[frameInfo.frameId] = true;
				}
			}

			if(WordIdSet[j]==OriwordId)
			{
				frameInfo.frameId = OriframeId;				
				if(!frameInputCheck[frameInfo.frameId]  && CostWeight[1][idx]!=0)
				{
					frameSet[j].push_back(frameInfo);
					frameInputCheck[frameInfo.frameId] = true;
				}
			}

			if(WordIdSet[j]==AccwordId)
			{
				frameInfo.frameId = AccframeId;
				if(!frameInputCheck[frameInfo.frameId]  && CostWeight[2][idx]!=0)
				{
					frameSet[j].push_back(frameInfo);
					frameInputCheck[frameInfo.frameId] = true;
				}
			}

            if(WordIdSet[j] == RssiwordId)
            {
                frameInfo.frameId = RssiframeId;
                if(!frameInputCheck[frameInfo.frameId]  && CostWeight[3][idx]!=0)
                {
                    frameSet[j].push_back(frameInfo);
                    frameInputCheck[frameInfo.frameId] = true;
                }
            }
            /// other features add below
		}
	}
	
	/************************************************************************/
	/*                         Error term                                   */
	/************************************************************************/
	//m_fp << "========================Energy Calculation=========================\n";
	float Ef[NUM_FEATURE+1]; 
	WE miniFrame;
	miniFrame.wordId = -1, miniFrame.frameId = -1, miniFrame.energyValue = 99999; 	
	for(int widx = 0 ; widx < frameSet.size() ; widx++)
	{
		int wordIdx  = frameSet[widx][0].wordId;

		//Get Word Weight
        float Wfa, Wori, Wacc, Wrssi;
        /// other features add below
		Wfa = m_FeatureWeight[wordIdx].GetWfa();
		Wori = m_FeatureWeight[wordIdx].GetWori();
		Wacc = m_FeatureWeight[wordIdx].GetWacc();
        Wrssi = m_FeatureWeight[wordIdx].GetWrssi();
        /// other features add below

		for(int fidx = 0 ; fidx < frameSet[widx].size() ; fidx++)
		{			
			int frameIdx = frameSet[widx][fidx].frameId;
			
            //Clear Ef to zeros
            for (int i = 0; i < NUM_FEATURE +1; i++)
            {
                Ef[i] = 0.0f;
            }

			//Calculate Energy Value
			for(int i = 0 ; i < K ; i++)
			{
				int FingerFid = m_finger_OLNG.m_Graph[M-1][i].SignClipInd;
				float disFa = SqrtDistance(m_database.m_oriSignData[frameIdx].m_readingData[0].m_fFingerAngle,
					                       m_database.m_oriSignData[frameIdx].m_readingData[1].m_fFingerAngle,
					                       m_database.m_oriSignData[FingerFid].m_readingData[0].m_fFingerAngle,
					                       m_database.m_oriSignData[FingerFid].m_readingData[1].m_fFingerAngle);

				int OriFid = m_quater_OLNG.m_Graph[M-1][i].SignClipInd;
				float disOri = SqrtDistance(m_database.m_oriSignData[frameIdx].m_readingData[0].m_qRotation,
					                        m_database.m_oriSignData[frameIdx].m_readingData[1].m_qRotation,
					                        m_database.m_oriSignData[OriFid].m_readingData[0].m_qRotation,
					                        m_database.m_oriSignData[OriFid].m_readingData[1].m_qRotation);

				int AccFid = m_accel_OLNG.m_Graph[M-1][i].SignClipInd;
				float disAcc = SqrtDistance(m_database.m_oriSignData[frameIdx].m_readingData[0].m_fPosition,
					                        m_database.m_oriSignData[frameIdx].m_readingData[1].m_fPosition,
					                        m_database.m_oriSignData[AccFid].m_readingData[0].m_fPosition,
					                        m_database.m_oriSignData[AccFid].m_readingData[1].m_fPosition);

                int RssiFid = m_rssi_OLNG.m_Graph[M-1][i].SignClipInd;
                float disRssi = SqrtDistance(m_database.m_oriSignData[frameIdx].m_readingData[0].m_fRssi,
                                             m_database.m_oriSignData[frameIdx].m_readingData[1].m_fRssi,
                                             m_database.m_oriSignData[RssiFid].m_readingData[0].m_fRssi,
                                             m_database.m_oriSignData[RssiFid].m_readingData[1].m_fRssi);
                /// other features add below

				Ef[0] += CostWeight[0][i] * disFa;
				Ef[1] += CostWeight[1][i] * disOri;
				Ef[2] += CostWeight[2][i] * disAcc;
                Ef[3] += CostWeight[3][i] * disRssi;
                /// other features add below
				Ef[4] += Math::Sqrt(disFa* disFa + disOri * disOri + disAcc * disAcc + disRssi * disRssi);
			}

            float energy = Wfa*Ef[0] + 2*Wori*Ef[1] + Wacc*Ef[2] + Wrssi*Ef[3];
            /// other features add below
			frameSet[widx][fidx].energyValue = energy;
			frameSet[widx][fidx].allDistance = Ef[NUM_FEATURE];
			localOptResult.push_back(frameSet[widx][fidx]);
			//Get minimum energy value frame and info
			if(miniFrame.energyValue > energy)
			{
				miniFrame.energyValue = energy;
				miniFrame.frameId     = frameSet[widx][fidx].frameId;
				miniFrame.wordId      = frameSet[widx][fidx].wordId;
			}
		}	
	}

	// 	m_fp << "========================Local Optimization=========================\n";
	// 	float miniAllDistance = 999999;
	// 	int   miniAllDisWid = -1;
	// 	int   miniAllDisFid = -1;
	// 	for(int i = 0 ; i < localOptResult.size() ; i++)
	// 	{
	// 		m_fp << m_database.m_signMotionFileName[localOptResult[i].wordId] <<" " << localOptResult[i].frameId << " " << localOptResult[i].energyValue << " " << localOptResult[i].allDistance << "\n";
	// 		if(miniAllDistance > localOptResult[i].allDistance)
	// 		{
	// 			miniAllDistance = localOptResult[i].allDistance;
	// 			miniAllDisWid = localOptResult[i].wordId;
	// 			miniAllDisFid = localOptResult[i].frameId;
	// 		}
	// 	}
	// 	m_fp << "==========================Minimum Cost=============================\n";
	// 	if(miniFrame.wordId!=-1)
	// 		m_fp << m_database.m_signMotionFileName[miniFrame.wordId] <<" " << miniFrame.frameId << " " << miniFrame.energyValue << "\n";
	// 	m_fp << "===================================================================\n";	
	// 	m_fp << "==========================Minimum All Distance=============================\n";
	// 	if(miniAllDisWid!=-1)
	// 		m_fp << m_database.m_signMotionFileName[miniAllDisWid] <<" " << miniAllDisFid << " " << miniAllDistance << "\n";
	// 	m_fp << "===================================================================\n";	


	//Release memory space
	std::vector<bool> EmpVec;
	FingerWord.swap(EmpVec);
	QuaterWord.swap(EmpVec);
	AccWord.swap(EmpVec);
    RssiWord.swap(EmpVec);
    /// other features add below
	frameInputCheck.swap(EmpVec);

	return localOptResult;
}

std::vector<WE> SLR_Manager::RssiLocalOptimization()
{
	/************************************************************************/
	/*                              Parameter                               */
	/************************************************************************/
	std::vector<WE> localOptResult;
	int K = m_finger_OLNG.K , M = m_finger_OLNG.M;


	//設定threshold以過濾掉過大的cost ==> 沒有參考價值得path
    float OLNGCostThreshold[NUM_FEATURE] = {0.0f}; // clear values to zero

	//Method 2 取final cost的平均
	for(int i = 0 ; i < K; i++)
	{
		OLNGCostThreshold[0] += m_finger_OLNG.m_finalCost[i];
		OLNGCostThreshold[1] += m_quater_OLNG.m_finalCost[i];
		OLNGCostThreshold[2] += m_accel_OLNG.m_finalCost[i];
        OLNGCostThreshold[3] += m_rssi_OLNG.m_finalCost[i];
        /// other features add below
	}

	/************************************************************************/
	/*                         Calculate cost weight                        */
	/************************************************************************/
	//step 1. find the maximum cost
	float maxCost[NUM_FEATURE] = {0.0f}; // clear values to zero
	for(int i = 0 ; i < K ; i++)
	{
        if(maxCost[3] < m_rssi_OLNG.m_finalCost[i] && m_rssi_OLNG.m_finalCost[i] < OLNGCostThreshold[3])
            maxCost[3] = m_rssi_OLNG.m_finalCost[i];
        /// other features add below
	}

	//step 2. sum all cost
	float deltaSum[NUM_FEATURE] = {0.0f};
	for(int i = 0 ; i < K ; i++)
	{
        if(m_rssi_OLNG.m_finalCost[i] < OLNGCostThreshold[3])
            deltaSum[3] += (maxCost[3] - m_rssi_OLNG.m_finalCost[i]);
        /// other features add below
	}
	//std::cout << "Delta Sum cost :" << deltaSum[0] << " " << deltaSum[1] << " " << deltaSum[2] << "\n";
	//step 3. Wi = (maxCost - cost[i])/deltaSum;
	//float CostWeight[3][40];
	float **CostWeight;
	CostWeight = new float*[NUM_FEATURE];
    for (int i = 0; i < NUM_FEATURE; i++)
    {
        CostWeight[i] = new float[K];
    }

	//m_fp << "==============================OLNG Cost ===============================\n";
	for(int i = 0 ; i < K ; i++)
	{
        //RSSI
        if(m_rssi_OLNG.m_finalCost[i] < OLNGCostThreshold[3])
            CostWeight[3][i] = (maxCost[3] - m_rssi_OLNG.m_finalCost[i])/deltaSum[3];
        /// other features add below
        else
            CostWeight[3][i] = 0;
		//m_fp << "cost weight : " << i << " " << CostWeight[0][i] << " " << CostWeight[1][i] << " " << CostWeight[2][i] << "\n";
	}
	//m_fp << "==========================================================================\n";

	/************************************************************************/
	/*                            Get OLNG Frame                            */
	/************************************************************************/
	std::vector<std::vector<WE>> frameSet;
	std::vector<int>  WordIdSet;
    std::vector<bool> RssiWord;
    /// other features add below
	int len = m_database.m_signMotionFileName.size();
    RssiWord.resize(len, false);
    /// other features add below
	std::vector<bool> frameInputCheck;
	frameInputCheck.resize(m_database.m_oriSignData.size(),false);
	
	for(int idx = 0 ; idx < K ; idx++)
	{
        //RSSI
        if(CostWeight[3][idx] != 0) //捨去過濾掉的frame
        {
            int RssiframeId = m_rssi_OLNG.m_Graph[m_rssi_OLNG.M-1][idx].SignClipInd;
            int RssiwordId  = m_database.m_oriSignData[RssiframeId].SignWordId;
            RssiWord[RssiwordId] = true;
        }
        /// other features add below
	}
	
	//clear local candidate word
	std::vector<int> emplocalword;
	emplocalword.swap(m_candidateWord_local);

	//找出OLNG圖結果中相同的字詞
	for(int idx = 0 ; idx < len ; idx++)
	{
        if(RssiWord[idx])
		{
            /// other features add below
			WordIdSet.push_back(idx);
			m_candidateWord_local.push_back(idx);
		}
	}
	
	frameSet.resize(WordIdSet.size());

	for(int idx = 0 ; idx < K ; idx++)
	{
        //RSSI
        int RssiframeId = m_rssi_OLNG.m_Graph[m_rssi_OLNG.M-1][idx].SignClipInd;
        int RssiwordId  = m_database.m_oriSignData[RssiframeId].SignWordId;
        /// other features add below
        
		//check the word is already in the candidate word or not.
		//if the word exist, then update the boundary.
		//放入不重複的frame到frameSet準備做計算
		WE frameInfo;		
		for(int j=0;j<WordIdSet.size();j++)
		{
			frameInfo.wordId = WordIdSet[j];

            if(WordIdSet[j] == RssiwordId)
            {
                frameInfo.frameId = RssiframeId;
                if(!frameInputCheck[frameInfo.frameId]  && CostWeight[3][idx]!=0)
                {
                    frameSet[j].push_back(frameInfo);
                    frameInputCheck[frameInfo.frameId] = true;
                }
            }
            /// other features add below
		}
	}
	
	/************************************************************************/
	/*                         Error term                                   */
	/************************************************************************/
	//m_fp << "========================Energy Calculation=========================\n";
	float Ef[NUM_FEATURE+1]; 
	WE miniFrame;
	miniFrame.wordId = -1, miniFrame.frameId = -1, miniFrame.energyValue = 99999; 	
	for(int widx = 0 ; widx < frameSet.size() ; widx++)
	{
		int wordIdx  = frameSet[widx][0].wordId;

		//Get Word Weight
        float Wfa, Wori, Wacc, Wrssi;
        /// other features add below
		Wfa = m_FeatureWeight[wordIdx].GetWfa();
		Wori = m_FeatureWeight[wordIdx].GetWori();
		Wacc = m_FeatureWeight[wordIdx].GetWacc();
        Wrssi = m_FeatureWeight[wordIdx].GetWrssi();
        /// other features add below

		for(int fidx = 0 ; fidx < frameSet[widx].size() ; fidx++)
		{			
			int frameIdx = frameSet[widx][fidx].frameId;
			
            //Clear Ef to zeros
            for (int i = 0; i < NUM_FEATURE +1; i++)
            {
                Ef[i] = 0.0f;
            }

			//Calculate Energy Value
			for(int i = 0 ; i < K ; i++)
			{
                int RssiFid = m_rssi_OLNG.m_Graph[M-1][i].SignClipInd;
                float disRssi = SqrtDistance(m_database.m_oriSignData[frameIdx].m_readingData[0].m_fRssi,
                                             m_database.m_oriSignData[frameIdx].m_readingData[1].m_fRssi,
                                             m_database.m_oriSignData[RssiFid].m_readingData[0].m_fRssi,
                                             m_database.m_oriSignData[RssiFid].m_readingData[1].m_fRssi);
                /// other features add below

				Ef[3] += CostWeight[3][i] * disRssi;
                /// other features add below
			}

            float energy = Wrssi * Ef[3];
            /// other features add below
            
			frameSet[widx][fidx].energyValue = energy;
			frameSet[widx][fidx].allDistance = Ef[3];
			localOptResult.push_back(frameSet[widx][fidx]);
			//Get minimum energy value frame and info
			if(miniFrame.energyValue > energy)
			{
				miniFrame.energyValue = energy;
				miniFrame.frameId     = frameSet[widx][fidx].frameId;
				miniFrame.wordId      = frameSet[widx][fidx].wordId;
			}
		}	
	}

	//Release memory space
	std::vector<bool> EmpVec;
    RssiWord.swap(EmpVec);
    /// other features add below
	frameInputCheck.swap(EmpVec);

	return localOptResult;
}

/**
*    @Function	
*    @Brief		
*/
std::vector<WE> SLR_Manager::IMUsLocalOptimization()
{
	m_fp << "IMU\n";
	/************************************************************************/
	/*                              Parameter                               */
	/************************************************************************/
	std::vector<WE> localOptResult;


	int K = m_finger_OLNG.K , M = m_finger_OLNG.M;
	//�]�wthreshold�H�L�o���L�j��cost ==> �S���Ѧһ��ȱopath
	float OLNGCostThreshold[3];
	OLNGCostThreshold[0] = OLNGCostThreshold[1] = OLNGCostThreshold[2] = 0;
	//Method 1 minimum final cost �M penaltySum �ۥ[��2
	// 	OLNGCostThreshold[0] = (m_finger_OLNG.m_minimumFinalCost + m_finger_OLNG.m_penaltySum) /2;
	// 	OLNGCostThreshold[1] = (m_quater_OLNG.m_minimumFinalCost + m_quater_OLNG.m_penaltySum) /2;
	// 	OLNGCostThreshold[2] = (m_accel_OLNG.m_minimumFinalCost + m_accel_OLNG.m_penaltySum) /2;
	//Method 2 ��final cost������
	for(int i = 0 ; i < K; i++)
	{
		OLNGCostThreshold[0] += m_finger_OLNG.m_finalCost[i];
		OLNGCostThreshold[1] += m_quater_OLNG.m_finalCost[i];
		OLNGCostThreshold[2] += m_accel_OLNG.m_finalCost[i] ;
	}
	OLNGCostThreshold[0] /= K;
	OLNGCostThreshold[1] /= K;
	OLNGCostThreshold[2] /= K;
	m_fp << "OLNGCostThreshold = "<< OLNGCostThreshold[0] << " " << OLNGCostThreshold[1] << " " << OLNGCostThreshold[2] << "\n";

	/************************************************************************/
	/*                         Calculate cost weight                        */
	/************************************************************************/
	//step 1. find the maximum cost
	float maxCost[3]={0};
	for(int i = 0 ; i < K ; i++)
	{
		if(maxCost[1] < m_quater_OLNG.m_finalCost[i] && m_quater_OLNG.m_finalCost[i] < OLNGCostThreshold[1])
			maxCost[1] = m_quater_OLNG.m_finalCost[i];

		if(maxCost[2] < m_accel_OLNG.m_finalCost[i] && m_accel_OLNG.m_finalCost[i] < OLNGCostThreshold[2])
			maxCost[2] = m_accel_OLNG.m_finalCost[i];
	}
	//std::cout << "Max cost :" << maxCost[0] << " " << maxCost[1] << " " << maxCost[2] << "\n";
	//step 2. sum all cost
	float deltaSum[3]={0};
	for(int i = 0 ; i < K ; i++)
	{
		if(m_quater_OLNG.m_finalCost[i] < OLNGCostThreshold[1])
			deltaSum[1] += (maxCost[1] - m_quater_OLNG.m_finalCost[i]);
		if(m_accel_OLNG.m_finalCost[i] < OLNGCostThreshold[2])
			deltaSum[2] += (maxCost[2] - m_accel_OLNG.m_finalCost[i]);
	}
	//std::cout << "Delta Sum cost :" << deltaSum[0] << " " << deltaSum[1] << " " << deltaSum[2] << "\n";
	//step 3. Wi = (maxCost - cost[i])/deltaSum;
	//float CostWeight[3][40];
	float **CostWeight;
	CostWeight = new float*[3];
	CostWeight[0] = new float[K];
	CostWeight[1] = new float[K];
	CostWeight[2] = new float[K];
	//m_fp << "==============================OLNG Cost ===============================\n";
	for(int i = 0 ; i < K; i++){
		//Orientation
		if(m_quater_OLNG.m_finalCost[i] < OLNGCostThreshold[1])
			CostWeight[1][i] = (maxCost[1] - m_quater_OLNG.m_finalCost[i])/deltaSum[1];
		else 
			CostWeight[1][i] = 0;

		//Acceleration
		if(m_accel_OLNG.m_finalCost[i] < OLNGCostThreshold[2])
			CostWeight[2][i] = (maxCost[2] - m_accel_OLNG.m_finalCost[i])/deltaSum[2];
		else
			CostWeight[2][i] = 0;
		//m_fp << "cost weight : " << i << " " << CostWeight[0][i] << " " << CostWeight[1][i] << " " << CostWeight[2][i] << "\n";
	}
	//m_fp << "==========================================================================\n";

	/************************************************************************/
	/*                            Get OLNG Frame                            */
	/************************************************************************/
	std::vector<std::vector<WE>> IMUsframeSet;
	std::vector<int>  IMUsWordIdSet;
	std::vector<bool> QuaterWord;
	std::vector<bool> AccWord;
	int len = m_database.m_signMotionFileName.size();
	QuaterWord.resize(len,false);
	AccWord.resize(len,false);
	std::vector<bool> IMUsframeInputCheck;
	IMUsframeInputCheck.resize(m_database.m_oriSignData.size(),false);

	for(int idx = 0 ; idx < K ; idx++)
	{
		//Orientation
		if(CostWeight[1][idx]!=0) //�˥h�L�o����frame
		{
			int OriframeId = m_quater_OLNG.m_Graph[m_quater_OLNG.M-1][idx].SignClipInd;
			int OriwordId  = m_database.m_oriSignData[OriframeId].SignWordId;
			QuaterWord[OriwordId] = true;
		}


		//Acceleration
		if(CostWeight[2][idx]!=0) //�˥h�L�o����frame
		{
			int AccframeId = m_accel_OLNG.m_Graph[m_accel_OLNG.M-1][idx].SignClipInd;
			int AccwordId  = m_database.m_oriSignData[AccframeId].SignWordId;
			AccWord[AccwordId] = true;
		}		
	}

	//clear local candidate word
	std::vector<int> emplocalword;
	emplocalword.swap(m_candidateWord_local);

	//��XOLNG�ϵ��G���ۦP���r��
	for(int idx = 0 ; idx < len ; idx++)
	{	
		if((QuaterWord[idx] && AccWord[idx]))
		{
			IMUsWordIdSet.push_back(idx);
			m_candidateWord_local.push_back(idx);
		}
	}

	//�D��X�����ƪ�frame
	IMUsframeSet.resize(IMUsWordIdSet.size());
	for(int idx = 0 ; idx < K ; idx++)
	{
		//Orientation
		int OriframeId = m_quater_OLNG.m_Graph[m_quater_OLNG.M-1][idx].SignClipInd;
		int OriwordId  = m_database.m_oriSignData[OriframeId].SignWordId;
		//Acceleration
		int AccframeId = m_accel_OLNG.m_Graph[m_accel_OLNG.M-1][idx].SignClipInd;
		int AccwordId  = m_database.m_oriSignData[AccframeId].SignWordId;

		//check the word is already in the candidate word or not.
		//if the word exist, then update the boundary.
		//��J�����ƪ�frame��frameSet�ǳư��p��
		WE frameInfo;		
		for(int j=0;j<IMUsWordIdSet.size();j++)
		{
			frameInfo.wordId = IMUsWordIdSet[j];
			if(IMUsWordIdSet[j]==OriwordId)
			{
				frameInfo.frameId = OriframeId;				
				if(!IMUsframeInputCheck[frameInfo.frameId]  && CostWeight[1][idx]!=0)
				{
					IMUsframeSet[j].push_back(frameInfo);
					IMUsframeInputCheck[frameInfo.frameId] = true;
				}
			}

			if(IMUsWordIdSet[j]==AccwordId)
			{
				frameInfo.frameId = AccframeId;
				if(!IMUsframeInputCheck[frameInfo.frameId]  && CostWeight[2][idx]!=0)
				{
					IMUsframeSet[j].push_back(frameInfo);
					IMUsframeInputCheck[frameInfo.frameId] = true;
				}
			}
		}
	}

	/************************************************************************/
	/*                         Error term                                   */
	/************************************************************************/
	m_fp << "========================Energy Calculation=========================\n";
	float Ef[4]; 
	WE miniFrame;
	miniFrame.wordId = -1, miniFrame.frameId = -1, miniFrame.energyValue = 99999; 	
	for(int widx = 0 ; widx < IMUsframeSet.size() ; widx++)
	{
		int wordIdx  = IMUsframeSet[widx][0].wordId;

		//Get Word Weight
		float Wori,Wacc;
		Wori = m_FeatureWeight[wordIdx].GetWori();
		Wacc = m_FeatureWeight[wordIdx].GetWacc();

		for(int fidx = 0 ; fidx < IMUsframeSet[widx].size() ; fidx++)
		{			

			int frameIdx = IMUsframeSet[widx][fidx].frameId;

			m_fp << m_database.m_signMotionFileName[wordIdx] <<" " << frameIdx << "\n";

			float fingerDis = 0;
			Ef[0] = Ef[1] = Ef[2] = Ef[3] = 0;

			//Calculate Energy Value
			for(int i = 0 ; i < K ; i++)
			{
				int OriFid = m_quater_OLNG.m_Graph[M-1][i].SignClipInd;
				float disOri = SqrtDistance(m_database.m_oriSignData[frameIdx].m_readingData[0].m_qRotation,
					m_database.m_oriSignData[frameIdx].m_readingData[1].m_qRotation,
					m_database.m_oriSignData[OriFid].m_readingData[0].m_qRotation,
					m_database.m_oriSignData[OriFid].m_readingData[1].m_qRotation);

				int AccFid = m_accel_OLNG.m_Graph[M-1][i].SignClipInd;
				float disAcc = SqrtDistance(m_database.m_oriSignData[frameIdx].m_readingData[0].m_fPosition,
					m_database.m_oriSignData[frameIdx].m_readingData[1].m_fPosition,
					m_database.m_oriSignData[AccFid].m_readingData[0].m_fPosition,
					m_database.m_oriSignData[AccFid].m_readingData[1].m_fPosition);				

				Ef[1] += CostWeight[1][i] * disOri;
				Ef[2] += CostWeight[2][i] * disAcc;
				Ef[3] += Math::Sqrt(disOri * disOri + disAcc * disAcc);
			}


			IMUsframeSet[widx][fidx].energyValue = 0.5f*Wori*Ef[1] + 2*Wacc*Ef[2];
			IMUsframeSet[widx][fidx].allDistance = Ef[3];
			localOptResult.push_back(IMUsframeSet[widx][fidx]);
			//Get minimum energy value frame and info
			if(miniFrame.energyValue > 0.5f*Wori*Ef[1] + 2*Wacc*Ef[2])
			{
				miniFrame.energyValue = 0.5f*Wori*Ef[1] + 2*Wacc*Ef[2];
				miniFrame.frameId     = IMUsframeSet[widx][fidx].frameId;
				miniFrame.wordId      = IMUsframeSet[widx][fidx].wordId;
				
			}
			//__android_log_print(ANDROID_LOG_DEBUG, "MIN", "EV: %f", miniFrame.energyValue);
		}	
	}

	m_fp << "========================Local Optimization=========================\n";
	float miniAllDistance = 999999;
	int   miniAllDisWid = -1;
	int   miniAllDisFid = -1;
	for(int i = 0 ; i < localOptResult.size() ; i++)
	{
		m_fp << m_database.m_signMotionFileName[localOptResult[i].wordId] <<" " << localOptResult[i].frameId << " " << localOptResult[i].energyValue << " " << localOptResult[i].allDistance << "\n";
		if(miniAllDistance > localOptResult[i].allDistance)
		{
			miniAllDistance = localOptResult[i].allDistance;
			miniAllDisWid = localOptResult[i].wordId;
			miniAllDisFid = localOptResult[i].frameId;
		}
	}
	m_fp << "==========================Minimum Cost=============================\n";
	if(miniFrame.wordId!=-1)
		m_fp << m_database.m_signMotionFileName[miniFrame.wordId] <<" " << miniFrame.frameId << " " << miniFrame.energyValue << "\n";
	m_fp << "===================================================================\n";	
	m_fp << "==========================Minimum All Distance=============================\n";
	if(miniAllDisWid!=-1)
		m_fp << m_database.m_signMotionFileName[miniAllDisWid] <<" " << miniAllDisFid << " " << miniAllDistance << "\n";
	m_fp << "===================================================================\n";	


	//Release memory space
	std::vector<bool> EmpVec;
	QuaterWord.swap(EmpVec);
	AccWord.swap(EmpVec);
	IMUsframeInputCheck.swap(EmpVec);

	return localOptResult;
}

/**
*    @Function	
*    @Brief		
*/
std::vector<WE> SLR_Manager::ACCLocalOptimization()
{
	/************************************************************************/
	/*                              Parameter                               */
	/************************************************************************/
	std::vector<WE> localOptResult;


	int K = m_finger_OLNG.K , M = m_finger_OLNG.M;
	//�]�wthreshold�H�L�o���L�j��cost ==> �S���Ѧһ��ȱopath
	float OLNGCostThreshold[3];
	OLNGCostThreshold[0] = OLNGCostThreshold[1] = OLNGCostThreshold[2] = 0;
	//Method 1 minimum final cost �M penaltySum �ۥ[��2
	// 	OLNGCostThreshold[0] = (m_finger_OLNG.m_minimumFinalCost + m_finger_OLNG.m_penaltySum) /2;
	// 	OLNGCostThreshold[1] = (m_quater_OLNG.m_minimumFinalCost + m_quater_OLNG.m_penaltySum) /2;
	// 	OLNGCostThreshold[2] = (m_accel_OLNG.m_minimumFinalCost + m_accel_OLNG.m_penaltySum) /2;
	//Method 2 ��final cost������
	for(int i = 0 ; i < K; i++)
	{
		OLNGCostThreshold[0] += m_finger_OLNG.m_finalCost[i];
		OLNGCostThreshold[1] += m_quater_OLNG.m_finalCost[i];
		OLNGCostThreshold[2] += m_accel_OLNG.m_finalCost[i] ;
	}
	// 	OLNGCostThreshold[0] /= K;
	// 	OLNGCostThreshold[1] /= K;
	// 	OLNGCostThreshold[2] /= K;
	// 	m_fp << "OLNGCostThreshold = "<< OLNGCostThreshold[0] << " " << OLNGCostThreshold[1] << " " << OLNGCostThreshold[2] << "\n";

	/************************************************************************/
	/*                         Calculate cost weight                        */
	/************************************************************************/
	//step 1. find the maximum cost
	float maxCost[3]={0};
	for(int i = 0 ; i < K ; i++)
	{
		if(maxCost[2] < m_accel_OLNG.m_finalCost[i] && m_accel_OLNG.m_finalCost[i] < OLNGCostThreshold[2])
			maxCost[2] = m_accel_OLNG.m_finalCost[i];
	}
	//std::cout << "Max cost :" << maxCost[0] << " " << maxCost[1] << " " << maxCost[2] << "\n";
	//step 2. sum all cost
	float deltaSum[3]={0};
	for(int i = 0 ; i < K ; i++)
	{
		if(m_accel_OLNG.m_finalCost[i] < OLNGCostThreshold[2])
			deltaSum[2] += (maxCost[2] - m_accel_OLNG.m_finalCost[i]);
	}
	//std::cout << "Delta Sum cost :" << deltaSum[0] << " " << deltaSum[1] << " " << deltaSum[2] << "\n";
	//step 3. Wi = (maxCost - cost[i])/deltaSum;
	//float CostWeight[3][40];
	float **CostWeight;
	CostWeight = new float*[3];
	CostWeight[0] = new float[K];
	CostWeight[1] = new float[K];
	CostWeight[2] = new float[K];
	//m_fp << "==============================OLNG Cost ===============================\n";
	for(int i = 0 ; i < K ; i++)
	{
		//Acceleration
		if(m_accel_OLNG.m_finalCost[i] < OLNGCostThreshold[2])
			CostWeight[2][i] = (maxCost[2] - m_accel_OLNG.m_finalCost[i])/deltaSum[2];
		else
			CostWeight[2][i] = 0;
		//m_fp << "cost weight : " << i << " " << CostWeight[0][i] << " " << CostWeight[1][i] << " " << CostWeight[2][i] << "\n";
	}
	//m_fp << "==========================================================================\n";

	/************************************************************************/
	/*                            Get OLNG Frame                            */
	/************************************************************************/
	std::vector<std::vector<WE>> ACCframeSet;
	std::vector<int>  ACCWordIdSet;
	std::vector<bool> AccWord;
	int len = m_database.m_signMotionFileName.size();
	AccWord.resize(len,false);
	std::vector<bool> ACCframeInputCheck;
	ACCframeInputCheck.resize(m_database.m_oriSignData.size(),false);

	for(int idx = 0 ; idx < K ; idx++)
	{
		//Acceleration
		if(CostWeight[2][idx]!=0) //�˥h�L�o����frame
		{
			int AccframeId = m_accel_OLNG.m_Graph[m_accel_OLNG.M-1][idx].SignClipInd;
			int AccwordId  = m_database.m_oriSignData[AccframeId].SignWordId;
			AccWord[AccwordId] = true;
		}		
	}

	//clear local candidate word
	std::vector<int> emplocalword;
	emplocalword.swap(m_candidateWord_local);

	//��XOLNG�ϵ��G���ۦP���r��
	for(int idx = 0 ; idx < len ; idx++)
	{	
		// Only Acc Version
		if(AccWord[idx])
		{
			ACCWordIdSet.push_back(idx);
			m_candidateWord_local.push_back(idx);
		}
	}

	ACCframeSet.resize(ACCWordIdSet.size());
	for(int idx = 0 ; idx < K ; idx++)
	{
		//Acceleration
		int AccframeId = m_accel_OLNG.m_Graph[m_accel_OLNG.M-1][idx].SignClipInd;
		int AccwordId  = m_database.m_oriSignData[AccframeId].SignWordId;

		//check the word is already in the candidate word or not.
		//if the word exist, then update the boundary.
		//��J�����ƪ�frame��frameSet�ǳư��p��
		WE frameInfo;		
		for(int j=0;j<ACCWordIdSet.size();j++)
		{
			frameInfo.wordId = ACCWordIdSet[j];
			if(ACCWordIdSet[j]==AccwordId)
			{
				frameInfo.frameId = AccframeId;
				if(!ACCframeInputCheck[frameInfo.frameId]  && CostWeight[2][idx]!=0)
				{
					ACCframeSet[j].push_back(frameInfo);
					ACCframeInputCheck[frameInfo.frameId] = true;
				}
			}
		}
	}

	/************************************************************************/
	/*                         Error term                                   */
	/************************************************************************/
	//m_fp << "========================Energy Calculation=========================\n";
	float Ef[4]; 
	WE miniFrame;
	miniFrame.wordId = -1, miniFrame.frameId = -1, miniFrame.energyValue = 99999; 	
	for(int widx = 0 ; widx < ACCframeSet.size() ; widx++)
	{
		int wordIdx  = ACCframeSet[widx][0].wordId;

		//Get Word Weight
		float Wfa,Wori,Wacc;
		Wfa = m_FeatureWeight[wordIdx].GetWfa();
		Wori = m_FeatureWeight[wordIdx].GetWori();
		Wacc = m_FeatureWeight[wordIdx].GetWacc();

		for(int fidx = 0 ; fidx < ACCframeSet[widx].size() ; fidx++)
		{			
			int frameIdx = ACCframeSet[widx][fidx].frameId;
			float fingerDis = 0;
			Ef[0] = Ef[1] = Ef[2] = Ef[3] = 0;

			//Calculate Energy Value
			for(int i = 0 ; i < K ; i++)
			{
				int AccFid = m_accel_OLNG.m_Graph[M-1][i].SignClipInd;
				float disAcc = SqrtDistance(m_database.m_oriSignData[frameIdx].m_readingData[0].m_fPosition,
					m_database.m_oriSignData[frameIdx].m_readingData[1].m_fPosition,
					m_database.m_oriSignData[AccFid].m_readingData[0].m_fPosition,
					m_database.m_oriSignData[AccFid].m_readingData[1].m_fPosition);				

				Ef[2] += CostWeight[2][i] * disAcc;
			}

			ACCframeSet[widx][fidx].energyValue = Wacc*Ef[2];
			ACCframeSet[widx][fidx].allDistance = Ef[2];
			localOptResult.push_back(ACCframeSet[widx][fidx]);
			//Get minimum energy value frame and info
			if(miniFrame.energyValue > Wacc*Ef[2])
			{
				miniFrame.energyValue = Wacc*Ef[2];
				miniFrame.frameId     = ACCframeSet[widx][fidx].frameId;
				miniFrame.wordId      = ACCframeSet[widx][fidx].wordId;				
			}
		}	
	}

	// 	m_fp << "========================Local Optimization=========================\n";
	// 	float miniAllDistance = 999999;
	// 	int   miniAllDisWid = -1;
	// 	int   miniAllDisFid = -1;
	// 	for(int i = 0 ; i < localOptResult.size() ; i++)
	// 	{
	// 		m_fp << m_database.m_signMotionFileName[localOptResult[i].wordId] <<" " << localOptResult[i].frameId << " " << localOptResult[i].energyValue << " " << localOptResult[i].allDistance << "\n";
	// 		if(miniAllDistance > localOptResult[i].allDistance)
	// 		{
	// 			miniAllDistance = localOptResult[i].allDistance;
	// 			miniAllDisWid = localOptResult[i].wordId;
	// 			miniAllDisFid = localOptResult[i].frameId;
	// 		}
	// 	}
	// 	m_fp << "==========================Minimum Cost=============================\n";
	// 	if(miniFrame.wordId!=-1)
	// 		m_fp << m_database.m_signMotionFileName[miniFrame.wordId] <<" " << miniFrame.frameId << " " << miniFrame.energyValue << "\n";
	// 	m_fp << "===================================================================\n";	
	// 	m_fp << "==========================Minimum All Distance=============================\n";
	// 	if(miniAllDisWid!=-1)
	// 		m_fp << m_database.m_signMotionFileName[miniAllDisWid] <<" " << miniAllDisFid << " " << miniAllDistance << "\n";
	// 	m_fp << "===================================================================\n";	


	//Release memory space
	std::vector<bool> EmpVec;
	AccWord.swap(EmpVec);
	ACCframeInputCheck.swap(EmpVec);

	return localOptResult;
}

/**
*    @Function	InitTimelineBox
*    @Brief		initialize m_GlobalMotionGraph_Final
*/
void SLR_Manager::InitTimelineBox()
{
	m_timeStep = 0;

	for(int t = 0 ; t < m_GlobalMotionGraph_Final.size() ; t++)
	{
		std::vector<WE> tmpV;
		tmpV.swap(m_GlobalMotionGraph_Final[t]);
	}
	std::vector<std::vector<WE>> tmpV;
	tmpV.swap(m_GlobalMotionGraph_Final);
	m_GlobalMotionGraph_Final.resize(OP2TimelineInterval);	
}

/**
*    @Function	UpdateTimelineBox
*    @Brief		�C�Ӯɶ��IT�p��final candidate word's frame �X�{���ƻPcost�֭p
*               ��XTimeBox�̭�showTime�̤j���r
*               �p�Gframe�ƭn�j��15��frame  ==> �O�dcandidate word
*/
void SLR_Manager::UpdateTimelineBox()
{
	//�C�Ӯɶ��IT�p��final candidate word's frame �X�{���ƻPcost�֭p

	int currTime = m_timeStep;

	//scan global motion graph at currTime and sum the cost and showTime
	for(int fidx = 0 ; fidx < m_GlobalMotionGraph_Final[currTime].size() ; fidx++)
	{
		for(int cadIdx = 0 ; cadIdx < m_candidateWord_Final.size() ; cadIdx++)
		{
			if(m_GlobalMotionGraph_Final[currTime][fidx].wordId == m_candidateWord_Final[cadIdx].wordId)
			{
				m_candidateWord_Final[cadIdx].showNum++;
				m_candidateWord_Final[cadIdx].energyValue += m_GlobalMotionGraph_Final[currTime][fidx].energyValue;
			}
		}
	}

//	__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::UpdateTimelineBox", "SS");
	
	if(m_timeStep==OP2TimelineInterval-1) 
	{
		//��XTimeBox�̭�showTime�̤j���r�Bframe�ƭn�j��15��frame  ==> �O�dcandidate word
		__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::UpdateTimelineBox", "=================================Final Result===================");
		int maxShowTime = -1;
		std::string opWord  = "";
		bool m_keepCandidate = false;
		for(int cadIdx = 0 ; cadIdx < m_candidateWord_Final.size() ; cadIdx++)
		{
			if(m_candidateWord_Final[cadIdx].showNum > maxShowTime && m_candidateWord_Final[cadIdx].showNum > OP2TIMEINTERVAL)
			{
				maxShowTime = m_candidateWord_Final[cadIdx].showNum;
				opWord  = m_database.m_signMotionFileName[m_candidateWord_Final[cadIdx].wordId];
			}	
			else if(m_candidateWord_Final[cadIdx].showNum > OP2TIMEINTERVAL/3)  //�O�dcandidate word��T
				m_keepCandidate = true;

			__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::UpdateTimelineBox", "word = %s(%d) ==> %f  showTime ==> %d", m_database.m_signMotionFileName[m_candidateWord_Final[cadIdx].wordId].c_str(), m_candidateWord_Final[cadIdx].wordId, m_candidateWord_Final[cadIdx].energyValue, m_candidateWord_Final[cadIdx].showNum);
		}
		__android_log_print(ANDROID_LOG_DEBUG, "SLR_Manager::UpdateTimelineBox", "================================================================");

		if(maxShowTime!=-1)
		{
			if(opWord!="Static.csv")
			{
				m_candidateWord = opWord;
				std::size_t dotId = m_candidateWord.find(".");
				std::string wordFilename = m_candidateWord.substr (0,dotId);
				m_candidateWord = wordFilename;

				if(m_lastTimeOutputWordId!=m_candidateWord)
				{
					m_lastTimeOutputWordId = m_candidateWord;				
					IsRecognition = true;
//					m_voice.FMODPlayAudio(wordFilename);
				}
			}				
			ClearCandidateWord();
		}

		InitTimelineBox();
		if(!m_keepCandidate)
			ClearCandidateWord();
	}
}

/**
*    @Function	ClearCandidateWord
*    @Brief		clear m_candidateWord_Final 
*/
void SLR_Manager::ClearCandidateWord()
{
	std::vector<WE> tmpWord;
	tmpWord.swap(m_candidateWord_Final);
}

/**
*    @Function	SqrtDistance
*    @Brief		calculate the cosine distance of finger angle
*/
float SLR_Manager::SqrtDistance(float* lfa,float* rfa,float* lfat,float* rfat){
	
	//Euclidean Distance
	/*
	float dis = 0;
	for(int i = 0 ; i < 10 ; i++)
	{
		dis += ((lfa[i] - lfat[i])/90) * ((lfa[i] - lfat[i])/90);
		dis += ((rfa[i] - rfat[i])/90) * ((rfa[i] - rfat[i])/90);
	}
	return Math::Sqrt(dis);
	*/

	//Cosine
	float dis = 0;
	float FaDot = 0;
	float FaSum[2] = {0};

	for(int i = 0 ; i < 10 ; i++)
	{
		FaDot+= lfa[i]*lfat[i];
		FaDot+= rfa[i]*rfat[i];
		FaSum[0] += (lfa[i] * lfa[i]);
		FaSum[0] += (rfa[i] * rfa[i]);
		FaSum[1] += (lfat[i] * lfat[i]);
		FaSum[1] += (rfat[i] * rfat[i]);
	}
	dis = FaDot / (Math::Sqrt(FaSum[0])*Math::Sqrt(FaSum[1]));

	return dis;
}

/**
*    @Function	SqrtDistance
*    @Brief		calculate the cosine distance of orientation
*/
float SLR_Manager::SqrtDistance(Quaternion lot,Quaternion rot,Quaternion lott,Quaternion rott){
	
	//Euclidean Distance
	/*
	float dis = 0;
	dis += (lot.w - lott.w)*(lot.w - lott.w) + (lot.x - lott.x)*(lot.x - lott.x) +
		(lot.y - lott.y)*(lot.y - lott.y) + (lot.z - lott.z)*(lot.z - lott.z);

	dis += (rot.w - rott.w)*(rot.w - rott.w) + (rot.x - rott.x)*(rot.x - rott.x) +
		(rot.y - rott.y)*(rot.y - rott.y) + (rot.z - rott.z)*(rot.z - rott.z);

	return Math::Sqrt(dis);
	*/

	//Cosine
	float dis = 0;
	float OriDot = 0;
	float OriSum[2] = {0};

	OriDot += lot.w * lott.w + lot.x * lott.x + lot.y * lott.y + lot.z * lott.z;
	OriDot += rot.w * rott.w + rot.x * rott.x + rot.y * rott.y + rot.z * rott.z;
	OriSum[0] = (lot.w * lot.w + lot.x * lot.x +lot.y * lot.y +lot.z * lot.z ) + (rot.w * rot.w + rot.x * rot.x +rot.y * rot.y +rot.z * rot.z);
	OriSum[1] = (lott.w * lott.w + lott.x * lott.x +lott.y * lott.y +lott.z * lott.z ) + (rott.w * rott.w + rott.x * rott.x +rott.y * rott.y +rott.z * rott.z);
	dis = OriDot / (Math::Sqrt(OriSum[0])*Math::Sqrt(OriSum[1]));

	return dis;
}

/**
*    @Function	SqrtDistance
*    @Brief		calculate the cosine distance of acceleration
*/
float SLR_Manager::SqrtDistance(Vector3 lacc,Vector3 racc,Vector3 lacct,Vector3 racct){
	//Euclidean Distance
	/*
	float dis = 0;
	dis += (lacc.x - lacct.x)*(lacc.x - lacct.x) + (lacc.y - lacct.y)*(lacc.y - lacct.y) + (lacc.z - lacct.z)*(lacc.z - lacct.z);

	dis += (racc.x - racct.x)*(racc.x - racct.x) + (racc.y - racct.y)*(racc.y - racct.y) + (racc.z - racct.z)*(racc.z - racc.z);

	return Math::Sqrt(dis);
	*/

	//Cosine
	float dis = 0;
	float AccDot = lacc.dotProduct(lacct) + racc.dotProduct(racct);
	dis = AccDot / (Math::Sqrt(lacc.length()*lacc.length() + racc.length()*racc.length())*Math::Sqrt(lacct.length()*lacct.length() + racct.length()*racct.length()));

	return dis;
}

float SLR_Manager::SqrtDistance(float lrssi, float rrssi, float lrssit, float rrssit)
{
    float RSSIdot = 0.0f;

    RSSIdot += lrssi * lrssit;
    RSSIdot += rrssi * rrssit;

    return RSSIdot / (Math::Sqrt(lrssi*lrssi + rrssi*rrssi) * Math::Sqrt(lrssit*lrssit + rrssit*rrssit));
}
/// other features add below
