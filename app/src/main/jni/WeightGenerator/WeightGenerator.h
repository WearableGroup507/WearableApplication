/************************************************************************
*    WeightGenerator.h
*    @Author Chin-Feng Chang
*	 @Brief  Calculate the vocabulary feature weight by computing the
*            standard deviation and average the sum of standard deviation
*			 for each feature
************************************************************************/

#ifndef WEIGHTGENERATOR_h
#define WEIGHTGENERATOR_h

//------------------------------------------------------------------------------
// Includes
//Sign Motion Database & Sign Motion Retrieval
#include "SignMotionData/SignMotionData.h"
#include <iomanip>

//------------------------------------------------------------------------------
// Class declaration

class WeightGenerator{
public:

	WeightGenerator():m_Wfa(1), m_Wori(1), m_Wacc(1), m_Wrssi(1)
    {
        Init();
    }

	void Init();                                      //initialize the computation 
	void Sum(float);                                  //sum up all feature value	
	void Average();                                   //calculate the average of m_sum	
	void StdDev(float);                               //calculate the standard deviation

	void  GenWeight();                                //generation the vocabulary feature weight for each feature
	float GenFaWeight();                              //generation the vocabulary feature weight for finger angle
	float GenOriWeight();                             //generation the vocabulary feature weight for orientation
	float GenAccWeight();                             //generation the vocabulary feature weight for acceleration
    float GenRssiWeight();                            //generation the vocabulary feature weight for RSSI
    /// other features add below

	void SetSize(int);                                //set the size of motion sequence of vocabulary of sign language
	void SetMotionSequence(std::vector<SignClip> ms); //set the motion sequence which need to calculate the feature weight
	void SetWeight(float Wfa,float Wori,float Wacc,float Wrssi);  //set weight value	

	float GetWfa(){return m_Wfa;}                     //get weight of finger angle	
	float GetWori(){return m_Wori;}                   //get weight of orientation	
	float GetWacc(){return m_Wacc;}                   //get weight of acceleration
    float GetWrssi(){return m_Wrssi;}                 //get weight of RSSI
    /// other features add below
	
	void  OutputWeight();                             //output the weight at the common line

private:
	float m_Wfa;                                      //feature weight of finger angle
	float m_Wori;                                     //feature weight of orientation
	float m_Wacc;                                     //feature weight of acceleration
    float m_Wrssi;                                    //feature weight of RSSI
    /// other features add below

	float m_count;                                    //counter 
	float m_size;                                     //the size of m_motionSequence
	float m_sum;                                      //the sum of feature value
	float m_average;                                  //the average of feature value
	float m_stdDev;                                   //the standard deviation of feauture
	std::vector<float> m_featureSet;
	std::vector<SignClip> m_motionSequence;           //temporal save the motion sequence need to be calculated the feature weight 
};
#endif
//------------------------------------------------------------------------------
// End of file
