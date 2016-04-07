/*
    WeightGenerator.cpp
    Author: Chin-Feng Chang
*/

//------------------------------------------------------------------------------
// Includes
#include "WeightGenerator/WeightGenerator.h"

//------------------------------------------------------------------------------
// Methods

/**
*    @Function	Init
*    @Brief		initialize the computation 
*/
void WeightGenerator::Init()
{
	m_count = m_sum = m_average = m_stdDev = 0;

	std::vector<float> emptyVec;
	emptyVec.swap(m_featureSet);
}


/**
*    @Function	SetSize
*    @Brief		set the size of motion sequence of vocabulary of sign language
*	 @Param		ss : The value is the size of motion sequence.
*/
void WeightGenerator::SetSize(int ss)
{
	m_size = ss;
}

/**
*    @Function	SetMotionSequence
*    @Brief		set the motion sequence which need to calculate the feature weight             
*/
void WeightGenerator::SetMotionSequence(std::vector<SignClip> ms)
{
	m_motionSequence = ms;
}

/**
*    @Function	SetWeight
*    @Brief   set weight value
*	 @param   Wfa   The weight of finger angle
*    @param   Wori  The weight of orientation
*	 @param   Wacc  The weight of acceleration
*    @param   Wrssi The weight of RSSI
*/
void WeightGenerator::SetWeight(float Wfa,float Wori,float Wacc,float Wrssi)
{
	m_Wfa = Wfa;
	m_Wori = Wori;
	m_Wacc = Wacc;
    m_Wrssi = Wrssi;
    /// other features add below
}

/**
*    @Function	Sum
*    @Brief		sum up all feature value
*/
void WeightGenerator::Sum(float mfeature)
{
	m_sum += mfeature;
}

/**
*    @Function	Average
*    @Brief		calculate the average of m_sum
*/
void WeightGenerator::Average()
{
	m_average = m_sum / m_size;
}

/**
*    @Function	StdDev
*    @Brief		calculate the standard deviation
*               {[sum (di - average)^2]/count}^0.5
*/
void WeightGenerator::StdDev(float m_feature)
{
	if(m_count < m_size)
	{
		Sum(m_feature);
		m_featureSet.push_back(m_feature);
		m_count++;
	}
	
	if(m_count == m_size)
	{		
		Average();
		//std::cout  <<  std::setprecision(20);
		//std::cout << "count = " << m_count << " Sum = " << m_sum  << " Average = " << m_average << "\n";
		//std::cout << "second:" << (m_featureSet[0]-m_average)*(m_featureSet[0]-m_average)*80 << "\n";
		for(int i = 0 ; i < m_size ; i++)
			m_stdDev+=(m_featureSet[i]-m_average)*(m_featureSet[i]-m_average);
		
		//std::cout << "third:" << m_stdDev << "\n";
		m_stdDev/=(m_size-1);

		m_stdDev = Math::Sqrt(m_stdDev);
	}
}

/**
*    @Function	GenWeight
*    @Brief		generation the vocabulary feature weight for each feature
*/
void WeightGenerator::GenWeight(){
	SetSize(m_motionSequence.size());
	
	//generate the weight of finger angle	
	m_Wfa = GenFaWeight();
	m_Wori = GenOriWeight();
	m_Wacc = GenAccWeight();
    m_Wrssi = GenRssiWeight();
    /// other features add below
}

/**
*    @Function	GenFaWeight
*    @Brief		generation the vocabulary feature weight for finger angle
*
*/
float WeightGenerator::GenFaWeight()
{
	float Wfa = 0;
	//scan all joint of left hand
	for(int Jid = 0 ; Jid < 10 ; Jid++)
	{
		//initial all parameter for calculate weight
		Init();

		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[0].m_fFingerAngle[Jid]);

		//sum up stand deviation 
		Wfa += m_stdDev;
	}

	//scan all joint of right hand
	for(int Jid = 0 ; Jid < 10 ; Jid++)
	{
		//initial all parameter for calculate weight
		Init();

		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[1].m_fFingerAngle[Jid]);

		//sum up stand deviation 
		Wfa += m_stdDev;
	}

	//average the sum of stand deviation 
	return Wfa/20;
}

/**
*    @Function	GenOriWeight
*    @Brief		generation the vocabulary feature weight for orientation
*/
float WeightGenerator::GenOriWeight()
{
	float Wori = 0;
	//std::cout << " Ori Std : ";
	//Left Hand
	{
		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[0].m_qRotation.w);
		Wori += m_stdDev;
		//std::cout << m_stdDev << ",";

		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[0].m_qRotation.x);
		Wori += m_stdDev;
		//std::cout << m_stdDev << ",";

		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[0].m_qRotation.y);
		Wori += m_stdDev;
		//std::cout << m_stdDev << ",";

		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[0].m_qRotation.z);
		Wori += m_stdDev;
		//std::cout << m_stdDev << ",";
	}

	//Right Hand
	{
		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[1].m_qRotation.w);
		Wori += m_stdDev;
		//std::cout << m_stdDev << ",";

		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[1].m_qRotation.x);
		Wori += m_stdDev;
		//std::cout << m_stdDev << ",";

		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[1].m_qRotation.y);
		Wori += m_stdDev;
		//std::cout << m_stdDev << ",";

		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[1].m_qRotation.z);
		Wori += m_stdDev;
		//std::cout << m_stdDev;
	}
	//std::cout << "\n";

	return Wori/8;
}

/**
*    @Function	GenAccWeight
*    @Brief		generation the vocabulary feature weight for acceleration
*/
float WeightGenerator::GenAccWeight()
{
	//std::cout << " Acc Std : ";
	float Wacc = 0;
	//Left Hand
	{
		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[0].m_fPosition.x);
			Wacc += m_stdDev;
			//std::cout << m_stdDev << ",";
		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[0].m_fPosition.y);
			Wacc += m_stdDev;
			//std::cout << m_stdDev << ",";
		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[0].m_fPosition.z);
			Wacc += m_stdDev;
			//std::cout << m_stdDev << ",";
	}

	//Right Hand
	{
		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[1].m_fPosition.x);
			Wacc += m_stdDev;
			//std::cout << m_stdDev << ",";

		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[1].m_fPosition.y);
			Wacc += m_stdDev;
			//std::cout << m_stdDev << ",";

		//initial all parameter for calculate weight
		Init();
		//scan all sequence
		for(int sz = 0 ; sz < m_size ; sz++)
			StdDev(m_motionSequence[sz].m_readingData[1].m_fPosition.z);
			Wacc += m_stdDev;
			//std::cout << m_stdDev;
	}
	//std::cout << "\n";
	return Wacc/6;
}

float WeightGenerator::GenRssiWeight()
{
    float Wrssi = 0;
    //Left Hand
    {
        //initial all parameter for calculate weight
        Init();
        //scan all sequence
        for(int sz = 0 ; sz < m_size ; sz++)
            StdDev(m_motionSequence[sz].m_readingData[LEFTt].m_fRssi);
        Wrssi += m_stdDev;
    }

    //Right Hand
    {
        //initial all parameter for calculate weight
        Init();
        //scan all sequence
        for(int sz = 0 ; sz < m_size ; sz++)
            StdDev(m_motionSequence[sz].m_readingData[RIGHT].m_fRssi);
        Wrssi += m_stdDev;
    }
    //std::cout << "\n";
    return Wrssi/2;
}

/**
*    @Function	OutputWeight
*    @Brief		output the weight at the common line
*/
void WeightGenerator::OutputWeight()
{
	std::cout << "Wfa," << m_Wfa << ",Wori," << m_Wori << ",Wacc," << m_Wacc << "\n";
}
//------------------------------------------------------------------------------
// End of file
